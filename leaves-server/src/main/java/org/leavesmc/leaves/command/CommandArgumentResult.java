package org.leavesmc.leaves.command;

import net.minecraft.core.BlockPos;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class CommandArgumentResult {

    private final List<Object> result;

    public CommandArgumentResult(List<Object> result) {
        this.result = result;
    }

    public int readInt(int def) {
        return Objects.requireNonNullElse(read(Integer.class), def);
    }

    public double readDouble(double def) {
        return Objects.requireNonNullElse(read(Double.class), def);
    }

    public float readFloat(float def) {
        return Objects.requireNonNullElse(read(Float.class), def);
    }

    public String readString(String def) {
        return Objects.requireNonNullElse(read(String.class), def);
    }

    public boolean readBoolean(boolean def) {
        return Objects.requireNonNullElse(read(Boolean.class), def);
    }

    public BlockPos readPos() {
        Integer[] pos = {read(Integer.class), read(Integer.class), read(Integer.class)};
        for (Integer po : pos) {
            if (po == null) {
                return null;
            }
        }
        return new BlockPos(pos[0], pos[1], pos[2]);
    }

    public @Nullable Vector readVector() {
        Double[] pos = {read(Double.class), read(Double.class), read(Double.class)};
        for (Double po : pos) {
            if (po == null) {
                return null;
            }
        }
        return new Vector(pos[0], pos[1], pos[2]);
    }

    public @NotNull Vector readVectorYZ(double x) {
        Double[] pos = {x, read(Double.class), read(Double.class)};
        for (Double po : pos) {
            if (po == null) {
                throw new IllegalArgumentException("Failed to read vector!");
            }
        }
        return new Vector(pos[0], pos[1], pos[2]);
    }

    public Object readObject() {
        if (result.isEmpty()) {
            return null;
        }
        return result.removeFirst();
    }

    public <T> T read(Class<T> tClass, T def) {
        return Objects.requireNonNullElse(read(tClass), def);
    }

    public <T> T read(Class<T> tClass) {
        if (result.isEmpty()) {
            return null;
        }

        Object obj = result.removeFirst();
        if (tClass.isInstance(obj)) {
            return tClass.cast(obj);
        } else {
            return null;
        }
    }
}
