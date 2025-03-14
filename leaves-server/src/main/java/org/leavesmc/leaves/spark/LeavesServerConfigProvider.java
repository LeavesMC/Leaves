package org.leavesmc.leaves.spark;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import me.lucko.spark.paper.common.platform.serverconfig.ConfigParser;
import me.lucko.spark.paper.common.platform.serverconfig.ExcludedConfigFilter;
import me.lucko.spark.paper.common.platform.serverconfig.PropertiesConfigParser;
import me.lucko.spark.paper.common.platform.serverconfig.ServerConfigProvider;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

// copy form https://github.com/lucko/spark/blob/master/spark-paper/src/main/java/me/lucko/spark/paper/PaperServerConfigProvider.java
public class LeavesServerConfigProvider extends ServerConfigProvider {
    private static final Map<String, ConfigParser> FILES;
    private static final Collection<String> HIDDEN_PATHS;

    public LeavesServerConfigProvider() {
        super(FILES, HIDDEN_PATHS);
    }

    private static class YamlConfigParser implements ConfigParser {
        public static final YamlConfigParser INSTANCE = new YamlConfigParser();
        protected static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(MemorySection.class, (JsonSerializer<MemorySection>) (obj, type, ctx) -> ctx.serialize(obj.getValues(false)))
            .create();

        @Override
        public JsonElement load(String file, ExcludedConfigFilter filter) throws IOException {
            Map<String, Object> values = this.parse(Paths.get(file));
            if (values == null) {
                return null;
            }

            return filter.apply(GSON.toJsonTree(values));
        }

        @Override
        public Map<String, Object> parse(BufferedReader reader) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
            return config.getValues(false);
        }
    }

    private static class SplitYamlConfigParser extends YamlConfigParser {
        public static final SplitYamlConfigParser INSTANCE = new SplitYamlConfigParser();

        @Override
        @Nullable
        public JsonElement load(@NotNull String group, ExcludedConfigFilter filter) throws IOException {
            String prefix = group.replace("/", "");

            Path configDir = Paths.get("config");
            if (!Files.exists(configDir)) {
                return null;
            }

            JsonObject root = new JsonObject();

            for (Map.Entry<String, Path> entry : getNestedFiles(configDir, prefix).entrySet()) {
                String fileName = entry.getKey();
                Path path = entry.getValue();

                Map<String, Object> values = this.parse(path);
                if (values == null) {
                    continue;
                }

                // apply the filter individually to each nested file
                root.add(fileName, filter.apply(GSON.toJsonTree(values)));
            }

            return root;
        }

        @NotNull
        private static Map<String, Path> getNestedFiles(@NotNull Path configDir, String prefix) {
            Map<String, Path> files = new LinkedHashMap<>();
            files.put("global.yml", configDir.resolve(prefix + "-global.yml"));
            files.put("world-defaults.yml", configDir.resolve(prefix + "-world-defaults.yml"));
            for (World world : Bukkit.getWorlds()) {
                files.put(world.getName() + ".yml", world.getWorldFolder().toPath().resolve(prefix + "-world.yml"));
            }
            return files;
        }
    }

    static {
        ImmutableMap.Builder<String, ConfigParser> files = ImmutableMap.<String, ConfigParser>builder()
            .put("server.properties", PropertiesConfigParser.INSTANCE)
            .put("bukkit.yml", YamlConfigParser.INSTANCE)
            .put("spigot.yml", YamlConfigParser.INSTANCE)
            .put("paper/", SplitYamlConfigParser.INSTANCE)
            .put("leaves.yml", YamlConfigParser.INSTANCE);

        for (String config : getSystemPropertyList("spark.serverconfigs.extra")) {
            files.put(config, YamlConfigParser.INSTANCE);
        }

        ImmutableSet.Builder<String> hiddenPaths = ImmutableSet.<String>builder()
            .add("database")
            .add("settings.bungeecord-addresses")
            .add("settings.velocity-support.secret")
            .add("proxies.velocity.secret")
            .add("server-ip")
            .add("motd")
            .add("resource-pack")
            .add("rcon<dot>password")
            .add("rcon<dot>ip")
            .add("level-seed")
            .add("world-settings.*.feature-seeds")
            .add("world-settings.*.seed-*")
            .add("feature-seeds")
            .add("seed-*")
            .addAll(getSystemPropertyList("spark.serverconfigs.hiddenpaths"));

        FILES = files.build();
        HIDDEN_PATHS = hiddenPaths.build();
    }
}
