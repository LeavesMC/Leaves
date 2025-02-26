package org.leavesmc.leaves.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.config.annotations.GlobalConfig;
import org.leavesmc.leaves.config.annotations.GlobalConfigCategory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;

import static org.leavesmc.leaves.config.GlobalConfigManager.CONFIG_START;

@SuppressWarnings("CallToPrintStackTrace")
public class GlobalConfigCreator {

    private static YamlConfiguration config;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) {
        config = new YamlConfiguration();
        config.options().setHeader(Collections.singletonList(LeavesConfig.CONFIG_HEADER));

        config.set("config-version", LeavesConfig.CURRENT_CONFIG_VERSION);

        for (Field field : LeavesConfig.class.getDeclaredFields()) {
            initField(field, null, CONFIG_START);
        }

        config.set("settings.protocol.xaero-map-server-id", 0);

        try {
            File file = new File("leaves.yml");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initCategory(@NotNull Field categoryField, @NotNull GlobalConfigCategory globalCategory, @Nullable Object upstreamField, @NotNull String upstreamPath) {
        try {
            Object category = categoryField.get(upstreamField);
            String categoryPath = upstreamPath + globalCategory.value() + ".";
            for (Field field : categoryField.getType().getDeclaredFields()) {
                initField(field, category, categoryPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initField(@NotNull Field field, @Nullable Object upstreamField, @NotNull String upstreamPath) {
        if (upstreamField != null || Modifier.isStatic(field.getModifiers())) {
            field.setAccessible(true);

            GlobalConfig globalConfig = field.getAnnotation(GlobalConfig.class);
            if (globalConfig != null) {
                initConfig(field, globalConfig, upstreamField, upstreamPath);
                return;
            }

            GlobalConfigCategory globalCategory = field.getType().getAnnotation(GlobalConfigCategory.class);
            if (globalCategory != null) {
                initCategory(field, globalCategory, upstreamField, upstreamPath);
            }
        }
    }


    private static void initConfig(@NotNull Field field, GlobalConfig globalConfig, @Nullable Object upstreamField, @NotNull String upstreamPath) {
        try {
            VerifiedConfig verifiedConfig = VerifiedConfig.build(globalConfig, field, upstreamField, upstreamPath);
            config.set(verifiedConfig.path(), verifiedConfig.validator().saveConvert(field.get(upstreamField)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
