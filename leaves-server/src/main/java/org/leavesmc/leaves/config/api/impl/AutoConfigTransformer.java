package org.leavesmc.leaves.config.api.impl;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.config.api.ConfigTransformer;
import org.leavesmc.leaves.config.api.ConfigValidator;

import java.lang.reflect.Field;

public class AutoConfigTransformer implements ConfigTransformer<Object, Object> {

    @NotNull
    @Contract("_ -> new")
    public static ConfigTransformer<?, ?> createValidator(@NotNull Field field) {
        return new SimpleConfigTransformer(AutoConfigValidator.createValidator(field));
    }

    @Override
    public Object transform(Object from) throws StopTransformException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SuppressWarnings({"unchecked", "rawtypes", "ClassCanBeRecord"})
    public static class SimpleConfigTransformer implements ConfigTransformer<Object, Object> {

        private final ConfigValidator validator;

        public SimpleConfigTransformer(ConfigValidator<?> validator) {
            this.validator = validator;
        }

        @Override
        public Object transform(Object o) throws StopTransformException {
            return o;
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
