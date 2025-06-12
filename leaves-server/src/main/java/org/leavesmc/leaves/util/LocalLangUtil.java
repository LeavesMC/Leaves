package org.leavesmc.leaves.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.locale.DeprecatedTranslationsInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class LocalLangUtil {
    final static Logger logger = Logger.getLogger("LangLoader");
    final static String VERSION = "1.21.5";
    final static String basePath = "cache/leaves/" + VERSION + "/";
    static String lang = org.leavesmc.leaves.LeavesConfig.mics.serverLang; // find in https://minecraft.wiki/w/Language, format: zh_cn ,en_us etc.
    static boolean isReloading = false;

    public static void init() {
        if (Objects.equals(lang, "en_us")) return;
        CompletableFuture.runAsync(() -> {
            try {
                String path = basePath + "lang/" + lang + ".json";
                if (!Files.exists(Path.of(path))) downloadLangAndCheck();
                try {
                    loadLocalLang(path);
                    isReloading = false;
                } catch (JsonParseException e) {
                    cleanCache();
                    if (!isReloading) {
                        isReloading = true;
                        init();
                    }
                }
            } catch (Exception e) {
                logger.severe(() -> "Async initialization failed: " + e.getMessage());
                isReloading = false;
            }
        });
    }

    private static void downloadLangAndCheck() {
        String path = basePath + VERSION + ".json";
        JsonObject json;
        if (Files.exists(Path.of(path))) {
            try {
                json = loadJson(path);
            } catch (Exception e) {
                logger.warning("Failed to load local JSON: " + e.getMessage());
                json = download();
            }
        } else {
            json = download();
        }

        if (json == null) {
            logger.warning("Failed to load language metadata");
            return;
        }

        try {
            JsonObject assetIndex = json.getAsJsonObject("assetIndex");
            String assetUrl = assetIndex.get("url").getAsString();
            byte[] assetData = fetchAndSave(assetUrl, basePath + "resource.json");

            JsonObject assets = JsonParser.parseString(new String(assetData)).getAsJsonObject();
            JsonObject langEntry = assets.getAsJsonObject("objects")
                    .getAsJsonObject("minecraft/lang/" + lang + ".json");

            String hash = langEntry.get("hash").getAsString();
            if (hash == null || hash.length() < 2) {
                throw new IllegalArgumentException("Invalid hash value");
            }

            downloadLang(hash);
        } catch (Exception e) {
            logger.warning("Asset processing failed: " + e.getMessage());
        }
    }

    private static void downloadLang(String hash) {
        String url = "https://resources.download.minecraft.net/"
                + hash.substring(0, 2) + "/" + hash;

        for (int i = 0; i < 3; i++) {
            try {
                fetchAndSave(url, basePath + "lang/" + lang + ".json");
                return;
            } catch (Exception e) {
                if (i == 2) logger.warning("Final attempt failed: " + e.getMessage());
            }
        }
    }

    private static JsonObject download() {
        String versionManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
        String targetVersionUrl = null;

        // Phase 1: Fetch version manifest
        for (int i = 0; i < 3; i++) {
            try (InputStreamReader reader = new InputStreamReader(
                    new ByteArrayInputStream(fetch(versionManifestUrl)))) {
                JsonObject manifest = JsonParser.parseReader(reader).getAsJsonObject();
                for (JsonElement element : manifest.getAsJsonArray("versions")) {
                    JsonObject version = element.getAsJsonObject();
                    if (VERSION.equals(version.get("id").getAsString())) {
                        targetVersionUrl = version.get("url").getAsString();
                        break;
                    }
                }
                if (targetVersionUrl != null) break;
            } catch (Exception e) {
                logger.warning("Failed to fetch version manifest: " + e.getMessage());
            }
        }

        if (targetVersionUrl == null) return null;

        // Phase 2: Fetch version metadata
        for (int i = 0; i < 3; i++) {
            try (InputStreamReader reader = new InputStreamReader(
                    new ByteArrayInputStream(fetchAndSave(targetVersionUrl, basePath + VERSION + ".json")))) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception e) {
                logger.warning("Failed to fetch version metadata: " + e.getMessage());
            }
        }
        return null;
    }

    public static byte[] fetch(String urlString) throws IOException {
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

    public static byte[] fetchAndSave(String url, String savePath) throws IOException {
        byte[] data = fetch(url);
        Path outputPath = Paths.get(savePath);
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, data);
        return data;
    }

    private static void cleanCache() {
        Path cachePath = Paths.get(basePath);
        try {
            if (Files.exists(cachePath)) {
                Files.walk(cachePath)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                logger.warning("Failed to delete: " + path + " - " + e.getMessage());
                            }
                        });
                logger.info("Cache cleaned: " + cachePath);
            } else {
                logger.info("Cache directory does not exist: " + cachePath);
            }
        } catch (IOException e) {
            logger.severe("Cache cleanup failed: " + e.getMessage());
        }
    }


    public static JsonObject loadJson(String path) throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(path));
        return JsonParser.parseString(new String(data)).getAsJsonObject();
    }

    public static void loadLocalLang(String lang) {
        try {
            Language.inject(load(lang));
        } catch (Exception e) {
            logger.warning("Failed to load language file for " + lang + "\n" + e);
            logger.info("Load default en_us instead of local lang " + lang);
            Language.inject(Language.loadDefault());
        }
    }

    private static Language load(String lang) {
        DeprecatedTranslationsInfo deprecatedTranslationsInfo = DeprecatedTranslationsInfo.loadFromDefaultResource();
        Map<String, String> map = new HashMap<>();
        BiConsumer<String, String> biConsumer = map::put;
        Language.parseTranslations(biConsumer, "/assets/minecraft/lang/en_us.json");
        parseTranslations(biConsumer, lang);
        deprecatedTranslationsInfo.applyToMap(map);
        final Map<String, String> map1 = Map.copyOf(map);
        return new Language() {
            @Override
            public String getOrDefault(String key, String defaultValue) {
                return map1.getOrDefault(key, defaultValue);
            }

            @Override
            public boolean has(String id) {
                return map1.containsKey(id);
            }

            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }

            @Override
            public FormattedCharSequence getVisualOrder(FormattedText text) {
                return sink -> text.visit(
                                (style, content) -> StringDecomposer.iterateFormatted(content, style, sink) ? Optional.empty() : FormattedText.STOP_ITERATION,
                                Style.EMPTY
                        )
                        .isPresent();
            }
        };
    }

    public static void parseTranslations(BiConsumer<String, String> output, String languagePath) {
        try {
            Path filePath = Paths.get(languagePath);
            try (InputStream fileStream = Files.newInputStream(filePath)) {
                Language.loadFromJson(fileStream, output);
            } catch (java.nio.file.NoSuchFileException fileEx) {
                logger.warning("Language file not found in both locations: " + languagePath);
            } catch (Exception fileEx) {
                logger.warning("Failed to load from filesystem " + filePath + "\n" + fileEx);
            }
        } catch (Exception ep) {
            logger.warning("Couldn't read strings from " + languagePath + "\n" + ep);
        }
    }
}