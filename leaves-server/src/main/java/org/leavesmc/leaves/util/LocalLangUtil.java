package org.leavesmc.leaves.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.locale.DeprecatedTranslationsInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class LocalLangUtil {

    private static final Logger logger = Logger.getLogger("LangLoader");
    private static final String VERSION = "1.21.5";
    private static final String BASE_PATH = "cache/leaves/" + VERSION + "/";
    private static final String manifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final String resourceBaseUrl = "https://resources.download.minecraft.net/";

    private static String langPath;
    private static String assetsPath;
    private static String versionPath;
    private static String manifestPath;
    private static String langJsonPath;

    public static void init() {
        if (Objects.equals(LeavesConfig.mics.serverLang, "en_us")) {
            return;
        }
        langPath = BASE_PATH + "lang/" + LeavesConfig.mics.serverLang + ".json";
        assetsPath = BASE_PATH + "assets.json";
        versionPath = BASE_PATH + VERSION + ".json";
        manifestPath = BASE_PATH + "manifest.json";
        langJsonPath = "minecraft/lang/" + LeavesConfig.mics.serverLang + ".json";
        logger.info("Starting load language: " + LeavesConfig.mics.serverLang);
        CompletableFuture.runAsync(() -> loadLocalLang(LeavesConfig.mics.serverLang, 2));
    }

    private static void loadLocalLang(String lang, int retryTime) {
        try {
            if (!Files.exists(Path.of(langPath))) {
                downloadLang(true);
            }
            Language.inject(createLangInstance());
            logger.info("Successfully loaded language: " + lang);
        } catch (Exception e) {
            logger.warning("Failed to load language file for " + lang + "\n" + e);
            if (retryTime > 0) {
                cleanCache();
                loadLocalLang(lang, retryTime - 1);
            } else {
                logger.severe("Failed to load for many times, use default lang \"en_us\" instead");
                cleanCache();
            }
        }
    }

    private static void downloadLang(boolean fetchFromAssets) throws Exception {
        JsonObject json;
        if (!Files.exists(Path.of(assetsPath)) || (json = loadJson(assetsPath)) == null) {
            if (fetchFromAssets) {
                downloadAssets(true);
                downloadLang(false);
            }
            return;
        }

        JsonObject langEntry = json.getAsJsonObject("objects").getAsJsonObject(langJsonPath);

        String hash = langEntry.get("hash").getAsString();
        if (hash == null || hash.length() < 2) {
            throw new IllegalArgumentException("Invalid hash value");
        }

        String langUrl = resourceBaseUrl + hash.substring(0, 2) + "/" + hash;
        fetchAndSave(langUrl, langPath);
    }

    private static void downloadAssets(boolean fetchFromVersion) throws Exception {
        JsonObject json;
        if (!Files.exists(Path.of(versionPath)) || (json = loadJson(versionPath)) == null) {
            if (fetchFromVersion) {
                downloadVersion(true);
                downloadAssets(false);
            }
            return;
        }

        JsonObject assetIndex = json.getAsJsonObject("assetIndex");
        String assetUrl = assetIndex.get("url").getAsString();
        fetchAndSave(assetUrl, assetsPath);
    }

    private static void downloadVersion(boolean fetchFromManifest) throws Exception {
        JsonObject json;
        if (!Files.exists(Path.of(manifestPath)) || (json = loadJson(manifestPath)) == null) {
            if (fetchFromManifest) {
                fetchAndSave(manifestUrl, manifestPath);
                downloadVersion(false);
            }
            return;
        }

        String versionUrl = null;
        for (JsonElement element : json.getAsJsonArray("versions")) {
            String id = element.getAsJsonObject().get("id").getAsString();
            String url = element.getAsJsonObject().get("url").getAsString();
            if (VERSION.equals(id)) {
                versionUrl = url;
                break;
            }
        }

        if (versionUrl == null) {
            throw new RuntimeException("Could not find version URL");
        }

        fetchAndSave(versionUrl, versionPath);
    }

    @SuppressWarnings("deprecation")
    private static byte[] fetch(String urlString) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        try (InputStream stream = conn.getInputStream()) {
            if (conn.getResponseCode() != 200) {
                throw new IOException("HTTP error: " + conn.getResponseCode());
            }
            return stream.readAllBytes();
        } finally {
            conn.disconnect();
        }
    }

    private static void fetchAndSave(String url, String savePath) throws IOException {
        byte[] data = fetch(url);
        Path outputPath = Path.of(savePath);
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, data, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    private static void cleanCache() {
        try {
            FileUtils.deleteDirectory(Path.of(BASE_PATH).toFile());
        } catch (IOException e) {
            logger.severe("Cache cleanup failed: " + e);
        }
    }

    private static JsonObject loadJson(String path) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(path));
            return JsonParser.parseString(new String(data)).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            logger.warning("Corrupt json file: " + e);
            throw e;
        } catch (Exception e) {
            logger.warning("Failed to load local JSON: " + e);
            return null;
        }
    }

    private static Language createLangInstance() throws IOException {
        DeprecatedTranslationsInfo deprecatedTranslationsInfo = DeprecatedTranslationsInfo.loadFromDefaultResource();
        Map<String, String> map = new HashMap<>();
        parseTranslations(map::put);
        deprecatedTranslationsInfo.applyToMap(map);
        final Map<String, String> map1 = Map.copyOf(map);
        return new Language() {
            @Override
            public @NotNull String getOrDefault(@NotNull String key, @NotNull String defaultValue) {
                return map1.getOrDefault(key, defaultValue);
            }

            @Override
            public boolean has(@NotNull String id) {
                return map1.containsKey(id);
            }

            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }

            @Override
            public @NotNull FormattedCharSequence getVisualOrder(@NotNull FormattedText text) {
                return sink -> text.visit(
                        (style, content) -> StringDecomposer.iterateFormatted(content, style, sink) ? Optional.empty() : FormattedText.STOP_ITERATION,
                        Style.EMPTY
                    )
                    .isPresent();
            }
        };
    }

    private static void parseTranslations(BiConsumer<String, String> output) throws IOException {
        Path filePath = Path.of(langPath);
        try (InputStream fileStream = Files.newInputStream(filePath)) {
            Language.loadFromJson(fileStream, output);
        } catch (NoSuchFileException noSuchFileException) {
            logger.warning("Couldn't find language file: " + langPath);
            throw noSuchFileException;
        } catch (Exception e) {
            logger.warning("Failed to load language from filesystem " + filePath + "\n" + e);
            throw e;
        }
    }
}