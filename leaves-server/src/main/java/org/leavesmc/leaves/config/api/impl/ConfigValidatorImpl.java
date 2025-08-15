package org.leavesmc.leaves.config.api.impl;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.config.api.ConfigValidator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ConfigValidatorImpl<E> implements ConfigValidator<E> {

    public static class BooleanConfigValidator extends ConfigValidatorImpl<Boolean> {
        @Override
        public Boolean stringConvert(String value) throws IllegalArgumentException {
            return Boolean.parseBoolean(value);
        }

        @Override
        public List<String> valueSuggest() {
            return List.of("false", "true");
        }
    }

    public static class IntConfigValidator extends ConfigValidatorImpl<Integer> {
        @Override
        public Integer stringConvert(String value) throws IllegalArgumentException {
            return Integer.parseInt(value);
        }
    }

    public static class LongConfigValidator extends ConfigValidatorImpl<Long> {
        @Override
        public Long stringConvert(String value) throws IllegalArgumentException {
            return Long.parseLong(value);
        }

        @Override
        public Long loadConvert(Object value) throws IllegalArgumentException {
            if (value instanceof Integer) {
                return Long.valueOf((Integer) value);
            }
            return (Long) value;
        }
    }

    public static class StringConfigValidator extends ConfigValidatorImpl<String> {
        @Override
        public String stringConvert(String value) throws IllegalArgumentException {
            return value;
        }
    }

    public static class DoubleConfigValidator extends ConfigValidatorImpl<Double> {
        @Override
        public Double stringConvert(String value) throws IllegalArgumentException {
            return Double.parseDouble(value);
        }
    }

    public abstract static class ListConfigValidator<E> extends ConfigValidatorImpl<List<E>> {

        public static class STRING extends ListConfigValidator<String> {
            public STRING() {
                super(new StringConfigValidator());
            }
        }

        public static class ENUM<E extends Enum<E>> extends ListConfigValidator<E> {
            public ENUM() {
                super(null);
                this.elementValidator = new EnumConfigValidator<E>(getTypeArgument(getClass(), ENUM.class, Class::isEnum));
            }

            public ENUM(@NotNull Class<E> enumClass) {
                super(new EnumConfigValidator<>(enumClass));
            }
        }

        protected ConfigValidator<E> elementValidator;

        public ListConfigValidator(ConfigValidator<E> elementValidator) {
            this.elementValidator = elementValidator;
        }

        @Override
        public List<E> loadConvert(Object value) throws IllegalArgumentException {
            if (elementValidator == null) {
                throw new IllegalArgumentException("element validator is null");
            }
            if (value instanceof List<?> list) {
                return list.stream().map(elementValidator::loadConvert).collect(Collectors.toList());
            } else {
                throw new IllegalArgumentException("value is not a list");
            }
        }

        @Override
        public Object saveConvert(List<E> value) {
            if (elementValidator == null) {
                throw new IllegalArgumentException("element validator is null");
            }
            return value.stream().map(elementValidator::saveConvert).collect(Collectors.toList());
        }

        @Override
        public List<E> stringConvert(String value) throws IllegalArgumentException {
            throw new IllegalArgumentException("not support"); // TODO
        }

        @Override
        public List<String> valueSuggest() {
            return List.of("<NOT SUPPORT>");
        }
    }

    public static class EnumConfigValidator<E extends Enum<E>> extends ConfigValidatorImpl<E> {

        protected Class<E> enumClass;
        private final List<String> enumValues;

        public EnumConfigValidator(@NotNull Class<E> enumClass) {
            this.enumClass = enumClass;
            this.enumValues = new ArrayList<>() {{
                for (E e : enumClass.getEnumConstants()) {
                    add(e.name().toLowerCase(Locale.ROOT));
                }
            }};
        }

        public EnumConfigValidator() {
            this.enumClass = getTypeArgument(getClass(), EnumConfigValidator.class, Class::isEnum);
            this.enumValues = new ArrayList<>() {{
                for (E e : enumClass.getEnumConstants()) {
                    add(e.name().toLowerCase(Locale.ROOT));
                }
            }};
        }

        @Override
        public E stringConvert(@NotNull String value) throws IllegalArgumentException {
            return Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
        }

        @Override
        public E loadConvert(@NotNull Object value) throws IllegalArgumentException {
            return this.stringConvert(value.toString());
        }

        @Override
        public Object saveConvert(@NotNull E value) {
            return value.toString().toUpperCase(Locale.ROOT);
        }

        @Override
        public List<String> valueSuggest() {
            return enumValues;
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> Class<E> getTypeArgument(Class<?> startClass, Class<?> rawType, Function<Class<?>, Boolean> check) {
        Type currentClass = startClass;
        while (currentClass instanceof Class<?> clazz) {
            Type genericSuperclass = clazz.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType parameterizedType) {
                if (rawType.equals(parameterizedType.getRawType())) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class<?> parameterizedClass && (check == null || check.apply(parameterizedClass))) {
                        return (Class<E>) parameterizedClass;
                    }
                }
            }
            currentClass = genericSuperclass;
        }
        throw new IllegalArgumentException("Can't find type argument of " + startClass.getName() + " for " + rawType.getName());
    }
}
