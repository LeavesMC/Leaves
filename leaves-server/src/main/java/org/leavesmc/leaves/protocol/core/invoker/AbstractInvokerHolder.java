package org.leavesmc.leaves.protocol.core.invoker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class AbstractInvokerHolder<T> {

    protected final LeavesProtocol owner;
    protected final Method invoker;
    protected final T handler;
    protected final Class<?> returnType;
    protected final Class<?>[] parameterTypes;

    protected AbstractInvokerHolder(LeavesProtocol owner, Method invoker, T handler, @Nullable Class<?> returnType, @NotNull Class<?>... parameterTypes) {
        this.owner = owner;
        this.invoker = invoker;
        this.handler = handler;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;

        validateMethodSignature();
    }

    protected void validateMethodSignature() {
        if (returnType != null && !returnType.isAssignableFrom(invoker.getReturnType())) {
            throw new IllegalArgumentException();
        }

        Class<?>[] methodParamTypes = invoker.getParameterTypes();
        if (methodParamTypes.length != parameterTypes.length) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (!methodParamTypes[i].isAssignableFrom(parameterTypes[i])) {
                throw new IllegalArgumentException();
            }
        }
    }

    public LeavesProtocol owner() {
        return owner;
    }

    public T handler() {
        return handler;
    }

    protected Object invoke0(boolean force, Object... args) {
        if (!force && !owner.isActive()) {
            return null;
        }
        try {
            if (Modifier.isStatic(invoker.getModifiers())) {
                return invoker.invoke(null, args);
            } else {
                return invoker.invoke(owner, args);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
