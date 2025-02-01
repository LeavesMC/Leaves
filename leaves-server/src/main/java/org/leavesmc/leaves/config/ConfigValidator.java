package org.leavesmc.leaves.config;

import java.util.List;

public interface ConfigValidator<E> extends ConfigConverter<E> {
    default void verify(E old, E value) throws IllegalArgumentException {
    }

    default List<String> valueSuggest() {
        return List.of("<value>");
    }

    default void runAfterLoader(E value, boolean firstLoad) {
    }
}
