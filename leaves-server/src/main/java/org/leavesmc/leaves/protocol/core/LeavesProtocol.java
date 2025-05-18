package org.leavesmc.leaves.protocol.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayDeque;
import java.util.Deque;

public interface LeavesProtocol {

    Deque<LeavesProtocol> reloadPending = new ArrayDeque<>(256);

    default void reload() {
        reloadPending.add(this);
    }

    boolean isActive();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Register {
        String namespace();
    }
}