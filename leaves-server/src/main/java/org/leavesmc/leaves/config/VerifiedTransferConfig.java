package org.leavesmc.leaves.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.config.annotations.TransferConfig;
import org.leavesmc.leaves.config.api.ConfigTransformer;
import org.leavesmc.leaves.config.api.impl.AutoConfigTransformer;

import java.lang.reflect.Field;

public record VerifiedTransferConfig(ConfigTransformer<? super Object, ? super Object> transformer, Field field, Object upstreamField, String path) {

    public void run() {
        if (LeavesConfig.config.contains(path)) {
            Object savedValue = LeavesConfig.config.get(path);
            if (savedValue != null) {
                try {
                    savedValue = transformer.loadConvert(savedValue);
                    savedValue = transformer.transform(savedValue);
                    field.set(upstreamField, savedValue);
                    LeavesConfig.config.set(path, null);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    LeavesLogger.LOGGER.warning("Failure to load leaves config" + path, e);
                } catch (ConfigTransformer.StopTransformException ignored) {
                }
            } else {
                LeavesLogger.LOGGER.warning("Failed to convert saved value for " + path + ", reset to default");
            }
        }
    }

    @Contract("_, _, _ -> new")
    public static @NotNull VerifiedTransferConfig build(@NotNull TransferConfig config, @NotNull Field field, @Nullable Object upstreamField) {
        String path = "settings." + config.value();

        ConfigTransformer<? super Object, ? super Object> transformer = null;
        try {
            transformer = createTransformer(config.transformer(), field);
        } catch (Exception e) {
            LeavesLogger.LOGGER.warning("Failure to load leaves config transformer for " + path, e);
        }

        return new VerifiedTransferConfig(transformer, field, upstreamField, path);
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
