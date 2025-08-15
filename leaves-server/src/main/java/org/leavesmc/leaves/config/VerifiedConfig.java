package org.leavesmc.leaves.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.config.annotations.GlobalConfig;
import org.leavesmc.leaves.config.api.ConfigValidator;
import org.leavesmc.leaves.config.api.impl.AutoConfigValidator;

import java.lang.reflect.Field;

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
                savedValue = validator.loadConvert(savedValue);
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
}
