package org.leavesmc.leaves.util;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.papermc.paper.ServerBuildInfo;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class LeavesUpdateHelper {

    private final static String autoUpdateDir = "auto_update";
    private final static String corePathFileName = autoUpdateDir + File.separator + "core.path";

    private final static ReentrantLock updateLock = new ReentrantLock();
    private static boolean updateTaskStarted = false;

    private static final ScheduledExecutorService autoUpdateExecutor = Executors.newScheduledThreadPool(1);

    public static void init() {
        File workingDirFile = new File(autoUpdateDir);
        if (!workingDirFile.exists()) {
            if (!workingDirFile.mkdir()) {
                LeavesLogger.LOGGER.warning("Failed to create working directory: " + autoUpdateDir);
            }
        }

        File corePathFile = new File(corePathFileName);
        if (!corePathFile.exists()) {
            try {
                if (!corePathFile.createNewFile()) {
                    throw new IOException();
                }
            } catch (IOException e) {
                LeavesLogger.LOGGER.severe("Failed to create core path file: " + corePathFileName, e);
            }
        }

        File leavesUpdateDir = new File(autoUpdateDir + File.separator + "leaves");
        if (!leavesUpdateDir.exists()) {
            if (!leavesUpdateDir.mkdir()) {
                LeavesLogger.LOGGER.warning("Failed to create leaves update directory: " + leavesUpdateDir);
            }
        }

        if (LeavesConfig.mics.autoUpdate.enable) {
            LocalTime currentTime = LocalTime.now();
            long dailyTaskPeriod = 24 * 60 * 60 * 1000;

            for (String time : LeavesConfig.mics.autoUpdate.updateTime) {
                try {
                    LocalTime taskTime = LocalTime.of(Integer.parseInt(time.split(":")[0]), Integer.parseInt(time.split(":")[1]));
                    Duration task = Duration.between(currentTime, taskTime);
                    if (task.isNegative()) {
                        task = task.plusDays(1);
                    }
                    autoUpdateExecutor.scheduleAtFixedRate(LeavesUpdateHelper::tryUpdateLeaves, task.toMillis(), dailyTaskPeriod, TimeUnit.MILLISECONDS);
                } catch (Exception ignored) {
                    LeavesLogger.LOGGER.warning("Illegal auto-update time ignored: " + time);
                }
            }
        }
    }

    public static void tryUpdateLeaves() {
        updateLock.lock();
        try {
            if (!updateTaskStarted) {
                updateTaskStarted = true;
                new Thread(LeavesUpdateHelper::downloadLeaves).start();
            }
        } finally {
            updateLock.unlock();
        }
    }

    private static void downloadLeaves() {
        ServerBuildInfo version = ServerBuildInfo.buildInfo();
        if (version.gitCommit().isEmpty() || version.buildNumber().isEmpty()) {
            LeavesLogger.LOGGER.info("IDE, custom build? Can not update!");
            updateTaskStarted = false;
            return;
        }

        LeavesLogger.LOGGER.info("Now gitHash: " + version.gitCommit().get());
        LeavesLogger.LOGGER.info("Trying to get latest build info.");
        LeavesBuildInfo buildInfo = getLatestBuildInfo(version.minecraftVersionId(), version.gitCommit().get());

        if (buildInfo != LeavesBuildInfo.ERROR) {
            if (!buildInfo.needUpdate) {
                LeavesLogger.LOGGER.warning("You are running the latest version, stopping update.");
                updateTaskStarted = false;
                return;
            }

            LeavesLogger.LOGGER.info("Got build info, trying to download " + buildInfo.fileName);
            try {
                Path outFile = Path.of(autoUpdateDir, "leaves", buildInfo.fileName + ".cache");
                Files.deleteIfExists(outFile);

                try (
                        final ReadableByteChannel source = Channels.newChannel(new URI(
                                buildInfo.url + LeavesConfig.mics.autoUpdate.source).toURL().openStream()
                        );
                        final FileChannel fileChannel = FileChannel.open(outFile, CREATE, WRITE, TRUNCATE_EXISTING)
                ) {
                    fileChannel.transferFrom(source, 0, Long.MAX_VALUE);
                    LeavesLogger.LOGGER.info("Download " + buildInfo.fileName + " completed.");
                } catch (final IOException e) {
                    LeavesLogger.LOGGER.warning("Download " + buildInfo.fileName + " failed.", e);
                    Files.deleteIfExists(outFile);
                    updateTaskStarted = false;
                    return;
                }

                if (!isFileValid(outFile, buildInfo.sha256)) {
                    LeavesLogger.LOGGER.warning("Hash check failed for downloaded file " + buildInfo.fileName);
                    Files.deleteIfExists(outFile);
                    updateTaskStarted = false;
                    return;
                }

                File nowServerCore = new File(autoUpdateDir + File.separator + "leaves" + File.separator + buildInfo.fileName);
                File backupServerCore = new File(autoUpdateDir + File.separator + "leaves" + File.separator + buildInfo.fileName + ".old");
                Util.safeReplaceFile(nowServerCore.toPath(), outFile, backupServerCore.toPath());

                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(corePathFileName))) {
                    bufferedWriter.write(autoUpdateDir + File.separator + "leaves" + File.separator + buildInfo.fileName);
                } catch (IOException e) {
                    LeavesLogger.LOGGER.warning("Fail to download leaves core", e);
                    updateTaskStarted = false;
                    return;
                }

                LeavesLogger.LOGGER.info("Leaves update completed, please restart your server.");
            } catch (Exception e) {
                LeavesLogger.LOGGER.severe("Leaves update failed", e);
            }
        } else {
            LeavesLogger.LOGGER.warning("Stopping update.");
        }
        updateTaskStarted = false;
    }

    private static boolean isFileValid(Path file, String hash) {
        try (FileInputStream inputStream = new FileInputStream(file.toFile())) {
            byte[] buffer = new byte[1024];
            MessageDigest md5 = MessageDigest.getInstance("SHA-256");

            for (int numRead; (numRead = inputStream.read(buffer)) > 0; ) {
                md5.update(buffer, 0, numRead);
            }

            return toHexString(md5.digest()).equals(hash);
        } catch (Exception e) {
            LeavesLogger.LOGGER.warning("Fail to validate file " + file, e);
        }
        return false;
    }

    @NotNull
    private static String toHexString(byte @NotNull [] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private static LeavesBuildInfo getLatestBuildInfo(String mcVersion, String gitHash) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(
                    "https://api.leavesmc.org/v2/projects/leaves/versions/" + mcVersion + "/builds/latest"
            ).toURL().openConnection();
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return LeavesBuildInfo.ERROR;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8))) {
                JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
                String channel = obj.get("channel").getAsString();
                if ("experimental".equals(channel) && !LeavesConfig.mics.autoUpdate.allowExperimental) {
                    LeavesLogger.LOGGER.warning("Experimental version is not allowed to update for default, if you really want to update, please set misc.auto-update.allow-experimental to true in leaves.yml");
                    return LeavesBuildInfo.ERROR;
                }
                int build = obj.get("build").getAsInt();

                JsonArray changes = obj.get("changes").getAsJsonArray();
                boolean needUpdate = true;
                for (JsonElement change : changes) {
                    if (change.getAsJsonObject().get("commit").getAsString().startsWith(gitHash)) {
                        needUpdate = false;
                        break;
                    }
                }

                JsonObject downloadInfo = obj.get("downloads").getAsJsonObject().get("application").getAsJsonObject();
                String fileName = downloadInfo.get("name").getAsString();
                String sha256 = downloadInfo.get("sha256").getAsString();
                String url = "https://api.leavesmc.org/v2/projects/leaves/versions/" + mcVersion + "/builds/" + build + "/downloads/";
                return new LeavesBuildInfo(build, fileName, sha256, needUpdate, url);
            } catch (JsonSyntaxException | NumberFormatException e) {
                LeavesLogger.LOGGER.warning("Fail to get latest build info", e);
                return LeavesBuildInfo.ERROR;
            }
        } catch (IOException | URISyntaxException e) {
            LeavesLogger.LOGGER.warning("Fail to get latest build info", e);
            return LeavesBuildInfo.ERROR;
        }
    }

    private record LeavesBuildInfo(int build, String fileName, String sha256, boolean needUpdate,  String url) {
        public static LeavesBuildInfo ERROR = null;
    }
}
