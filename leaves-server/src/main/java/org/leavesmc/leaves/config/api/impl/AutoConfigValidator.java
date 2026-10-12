package org.leavesmc.leaves.config.api.impl;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.config.api.ConfigValidator;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class AutoConfigValidator implements ConfigValidator<Object> {

    private static final Map<Class<?>, Supplier<ConfigValidator<?>>> BASE_VALIDATOR = new HashMap<>();

    static {
        BASE_VALIDATOR.put(Boolean.class, ConfigValidatorImpl.BooleanConfigValidator::new);
        BASE_VALIDATOR.put(boolean.class, ConfigValidatorImpl.BooleanConfigValidator::new);

        BASE_VALIDATOR.put(Integer.class, ConfigValidatorImpl.IntConfigValidator::new);
        BASE_VALIDATOR.put(int.class, ConfigValidatorImpl.IntConfigValidator::new);

        BASE_VALIDATOR.put(Long.class, ConfigValidatorImpl.LongConfigValidator::new);
        BASE_VALIDATOR.put(long.class, ConfigValidatorImpl.LongConfigValidator::new);

        BASE_VALIDATOR.put(Double.class, ConfigValidatorImpl.DoubleConfigValidator::new);
        BASE_VALIDATOR.put(double.class, ConfigValidatorImpl.DoubleConfigValidator::new);

        BASE_VALIDATOR.put(String.class, ConfigValidatorImpl.StringConfigValidator::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ConfigValidator<?> createValidator(@NotNull Field field) {
        Class<?> fieldType = field.getType();

        if (BASE_VALIDATOR.containsKey(fieldType)) {
            return BASE_VALIDATOR.get(fieldType).get();
        }

        if (List.class.isAssignableFrom(fieldType)) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType parameterizedType) {
                Type[] typeArgs = parameterizedType.getActualTypeArguments();
                if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> genericClass) {
                    if (genericClass.equals(String.class)) {
                        return new ConfigValidatorImpl.ListConfigValidator.STRING();
                    } else if (genericClass.isEnum()) {
                        return new ConfigValidatorImpl.ListConfigValidator.ENUM<>((Class<Enum>) genericClass);
                    }
                    throw new IllegalArgumentException("List type " + genericClass.getTypeName() + " is not supported.");
                }
            }
            throw new IllegalArgumentException("List type " + genericType.getTypeName() + " is not supported.");
        }

        if (fieldType.isEnum()) {
            return new ConfigValidatorImpl.EnumConfigValidator<>((Class<Enum>) fieldType);
        }

        throw new IllegalArgumentException("Can't find validator for type " + fieldType.getName());
    }

    @Override
    public Object stringConvert(String value) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
