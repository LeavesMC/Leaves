package org.leavesmc.leaves.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;

import java.lang.reflect.Field;

public record VerifiedRemovedConfig(ConfigTransformer<? super Object, ? super Object> transformer, boolean transform, Field field, Object upstreamField, String path) {

    public void run() {
        if (transform) {
            if (LeavesConfig.config.contains(path)) {
                Object savedValue = LeavesConfig.config.get(path);
                if (savedValue != null) {
                    try {
                        savedValue = transformer.loadConvert(savedValue);
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
    public static @NotNull VerifiedRemovedConfig build(@NotNull org.leavesmc.leaves.config.annotations.RemovedConfig config, @NotNull Field field, @Nullable Object upstreamField) {
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

        return new VerifiedRemovedConfig(transformer, config.transform(), field, upstreamField, path.toString());
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
