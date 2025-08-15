package org.leavesmc.leaves.config.api;

import java.util.List;

public interface ConfigValidator<E> extends ConfigConverter<E> {
    E stringConvert(String value) throws IllegalArgumentException;

    default void verify(E old, E value) throws IllegalArgumentException {
    }

    default List<String> valueSuggest() {
        return List.of("<value>");
    }

    default void runAfterLoader(E value, boolean reload) {
    }
}
