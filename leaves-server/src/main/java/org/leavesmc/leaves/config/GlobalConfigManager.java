package org.leavesmc.leaves.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GlobalConfigManager {

    public final static String CONFIG_START = "settings.";

    private static boolean firstLoad = true;
    private static final Map<String, VerifiedConfig> verifiedConfigs = new HashMap<>();

    public static void init() {
        verifiedConfigs.clear();

        for (Field field : LeavesConfig.class.getDeclaredFields()) {
            initField(field, null, CONFIG_START);
        }

        verifiedConfigs.forEach((path, config) -> config.validator.runAfterLoader(config.get(), firstLoad));

        firstLoad = false;
        LeavesConfig.save();
    }

    private static void initCategory(@NotNull Field categoryField, @NotNull GlobalConfigCategory globalCategory, @Nullable Object upstreamField, @NotNull String upstreamPath) {
        try {
            Object category = categoryField.get(upstreamField);
            String categoryPath = upstreamPath + globalCategory.value() + ".";
            for (Field field : categoryField.getType().getDeclaredFields()) {
                initField(field, category, categoryPath);
            }
        } catch (Exception e) {
            LeavesLogger.LOGGER.severe("Failure to load leaves config" + upstreamPath, e);
        }
    }

    private static void initField(@NotNull Field field, @Nullable Object upstreamField, @NotNull String upstreamPath) {
        if (upstreamField != null || Modifier.isStatic(field.getModifiers())) {
            field.setAccessible(true);

            for (RemovedConfig config : field.getAnnotationsByType(RemovedConfig.class)) {
                RemovedVerifiedConfig.build(config, field, upstreamField).run();
            }

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

            if (globalConfig.lock() && !firstLoad) {
                verifiedConfigs.put(verifiedConfig.path.substring(CONFIG_START.length()), verifiedConfig);
            }

            ConfigValidator<? super Object> validator = verifiedConfig.validator;

            Object defValue = validator.saveConvert(field.get(upstreamField));
            LeavesConfig.config.addDefault(verifiedConfig.path, defValue);

            try {
                Object savedValue = LeavesConfig.config.get(verifiedConfig.path);
                if (savedValue == null) {
                    throw new IllegalArgumentException("?");
                }

                if (savedValue.getClass() != validator.getFieldClass()) {
                    savedValue = validator.loadConvert(savedValue);
                }

                validator.verify(null, savedValue);

                field.set(upstreamField, savedValue);
            } catch (IllegalArgumentException | ClassCastException e) {
                LeavesConfig.config.set(verifiedConfig.path, defValue);
                LeavesLogger.LOGGER.warning(e.getMessage() + ", reset to " + defValue);
            }

            verifiedConfigs.put(verifiedConfig.path.substring(CONFIG_START.length()), verifiedConfig);
        } catch (Exception e) {
            LeavesLogger.LOGGER.severe("Failure to load leaves config", e);
            throw new RuntimeException();
        }
    }

    public static VerifiedConfig getVerifiedConfig(String path) {
        return verifiedConfigs.get(path);
    }

    @Contract(pure = true)
    public static @NotNull Set<String> getVerifiedConfigPaths() {
        return verifiedConfigs.keySet();
    }

    public record RemovedVerifiedConfig(ConfigTransformer<? super Object, ? super Object> transformer, boolean transform, Field field, Object upstreamField, String path) {

        public void run() {
            if (transform) {
                if (LeavesConfig.config.contains(path)) {
                    Object savedValue = LeavesConfig.config.get(path);
                    if (savedValue != null) {
                        try {
                            if (savedValue.getClass() != transformer.getFieldClass()) {
                                savedValue = transformer.loadConvert(savedValue);
                            }
                            savedValue = transformer.transform(savedValue);
                            field.set(upstreamField, savedValue);
                        } catch (IllegalAccessException | IllegalArgumentException e) {
                            LeavesLogger.LOGGER.warning("Failure to load leaves config" + path, e);
                        }
                    } else {
                        LeavesLogger.LOGGER.warning("Failed to convert saved value for " + path + ", reset to default");
                    }
                }
            }
            LeavesConfig.config.set(path, null);
        }

        @Contract("_, _, _ -> new")
        public static @NotNull RemovedVerifiedConfig build(@NotNull RemovedConfig config, @NotNull Field field, @Nullable Object upstreamField) {
            StringBuilder path = new StringBuilder("settings.");
            for (String category : config.category()) {
                path.append(category).append(".");
            }
            path.append(config.name());

            ConfigTransformer<? super Object, ? super Object> transformer = null;
            try {
                transformer = createTransformer(config.transformer(), field);
            } catch (Exception e) {
                LeavesLogger.LOGGER.warning("Failure to load leaves config" + path, e);
            }

            return new RemovedVerifiedConfig(transformer, config.transform(), field, upstreamField, path.toString());
        }
    }

    public record VerifiedConfig(ConfigValidator<? super Object> validator, boolean lock, Field field, Object upstreamField, String path) {

        public void set(String stringValue) throws IllegalArgumentException {
            Object value;
            try {
                value = validator.stringConvert(stringValue);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("value parse error: " + e.getMessage());
            }

            validator.verify(this.get(), value);

            try {
                LeavesConfig.config.set(path, validator.saveConvert(value));
                LeavesConfig.save();
                if (lock) {
                    throw new IllegalArgumentException("locked, will load after restart");
                }
                field.set(upstreamField, value);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }

        public Object get() {
            try {
                return field.get(upstreamField);
            } catch (IllegalAccessException e) {
                LeavesLogger.LOGGER.severe("Failure to get " + path + " value", e);
                return "<VALUE ERROR>";
            }
        }

        public String getString() {
            Object value = this.get();

            Object savedValue = LeavesConfig.config.get(path);
            try {
                if (savedValue != null) {
                    if (validator.getFieldClass() != savedValue.getClass()) {
                        savedValue = validator.loadConvert(savedValue);
                    }

                    if (!savedValue.equals(value)) {
                        return value.toString() + "(" + savedValue + " after restart)";
                    }
                }
            } catch (IllegalArgumentException e) {
                LeavesLogger.LOGGER.severe("Failure to get " + path + " value", e);
            }
            return value.toString();
        }

        @NotNull
        @Contract("_, _, _, _ -> new")
        public static VerifiedConfig build(@NotNull GlobalConfig config, @NotNull Field field, @Nullable Object upstreamField, @NotNull String upstreamPath) {
            String path = upstreamPath + config.value();

            ConfigValidator<? super Object> validator;
            try {
                validator = createValidator(config.validator(), field);
            } catch (Exception e) {
                LeavesLogger.LOGGER.severe("Failure to load leaves config" + path, e);
                throw new RuntimeException();
            }

            return new VerifiedConfig(validator, config.lock(), field, upstreamField, path);
        }
    }

    @SuppressWarnings("unchecked")
    private static ConfigValidator<? super Object> createValidator(@NotNull Class<? extends ConfigValidator<?>> clazz, Field field) throws Exception {
        if (clazz.equals(AutoConfigValidator.class)) {
            return (ConfigValidator<? super Object>) AutoConfigValidator.createValidator(field);
        } else {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (ConfigValidator<? super Object>) constructor.newInstance();
        }
    }

    @SuppressWarnings("unchecked")
    private static ConfigTransformer<? super Object, ? super Object> createTransformer(@NotNull Class<? extends ConfigTransformer<?, ?>> clazz, Field field) throws Exception {
        if (clazz.equals(AutoConfigTransformer.class)) {
            return (ConfigTransformer<? super Object, ? super Object>) AutoConfigTransformer.createValidator(field);
        } else {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (ConfigTransformer<? super Object, ? super Object>) constructor.newInstance();
        }
    }
}
