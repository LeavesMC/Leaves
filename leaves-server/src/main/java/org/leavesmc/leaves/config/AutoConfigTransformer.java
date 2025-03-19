package org.leavesmc.leaves.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class AutoConfigTransformer implements ConfigTransformer<Object, Object> {

    @NotNull
    @Contract("_ -> new")
    public static ConfigTransformer<?, ?> createValidator(@NotNull Field field) {
        return new SimpleConfigTransformer(AutoConfigValidator.createValidator(field));
    }

    @Override
    public Object transform(Object from) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object stringConvert(String value) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static class SimpleConfigTransformer implements ConfigTransformer<Object, Object> {

        private final ConfigValidator validator;

        public SimpleConfigTransformer(ConfigValidator<?> validator) {
            this.validator = validator;
        }

        @Override
        public Object transform(Object o) {
            return o;
        }

        @Override
        public Object stringConvert(String value) throws IllegalArgumentException {
            return validator.stringConvert(value);
        }

        @Override
        public Object loadConvert(Object value) throws IllegalArgumentException {
            return validator.loadConvert(value);
        }

        @Override
        public Object saveConvert(Object value) {
            return validator.saveConvert(value);
        }
    }
}
