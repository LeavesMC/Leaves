package org.leavesmc.leaves.config;

import org.jetbrains.annotations.NotNull;

public record LeavesConfigValue(Object value) {

    public int getInt() {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        throw new ClassCastException("Value is not an integer");
    }

    public double getDouble() {
        if (value instanceof Double) {
            return (Double) value;
        }
        throw new ClassCastException("Value is not a double");
    }

    public boolean getBoolean() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new ClassCastException("Value is not a boolean");
    }

    public String getString() {
        if (value instanceof String) {
            return (String) value;
        }
        throw new ClassCastException("Value is not a string");
    }

    public @NotNull String toString() {
        return value.toString();
    }
}
