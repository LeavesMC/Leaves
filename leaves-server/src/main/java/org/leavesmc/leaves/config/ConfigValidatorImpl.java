package org.leavesmc.leaves.config;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class ConfigValidatorImpl<E> implements ConfigValidator<E> {

    protected Class<E> fieldClass;

    @SuppressWarnings("unchecked")
    public ConfigValidatorImpl() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) superClass).getActualTypeArguments();
            if (actualTypeArguments[0] instanceof Class) {
                this.fieldClass = (Class<E>) actualTypeArguments[0];
            }
        }
    }

    @Override
    public Class<E> getFieldClass() {
        return fieldClass;
    }

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
        }

        @Override
        public List<E> stringConvert(String value) throws IllegalArgumentException {
            throw new IllegalArgumentException("not support"); // TODO
        }
    }

    public static class EnumConfigValidator<E extends Enum<E>> extends ConfigValidatorImpl<E> {

        private final List<String> enumValues;

        public EnumConfigValidator(@NotNull Class<E> enumClass) {
            this.fieldClass = enumClass;
            this.enumValues = new ArrayList<>() {{
                for (E e : enumClass.getEnumConstants()) {
                    add(e.name().toLowerCase(Locale.ROOT));
                }
            }};
        }

        public EnumConfigValidator() {
            this.enumValues = new ArrayList<>() {{
                for (E e : fieldClass.getEnumConstants()) {
                    add(e.name().toLowerCase(Locale.ROOT));
                }
            }};
        }

        @Override
        public E stringConvert(@NotNull String value) throws IllegalArgumentException {
            return Enum.valueOf(getFieldClass(), value.toUpperCase(Locale.ROOT));
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
}
