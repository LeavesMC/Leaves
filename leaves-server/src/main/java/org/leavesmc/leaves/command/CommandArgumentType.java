package org.leavesmc.leaves.command;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class CommandArgumentType<E> {

    public static final CommandArgumentType<String> STRING = CommandArgumentType.string();
    public static final CommandArgumentType<Integer> INTEGER = CommandArgumentType.of(Integer.class, Integer::parseInt);
    public static final CommandArgumentType<Double> DOUBLE = CommandArgumentType.of(Double.class, Double::parseDouble);
    public static final CommandArgumentType<Float> FLOAT = CommandArgumentType.of(Float.class, Float::parseFloat);
    public static final CommandArgumentType<Boolean> BOOLEAN = CommandArgumentType.of(Boolean.class, Boolean::parseBoolean);

    private final Class<E> type;

    private CommandArgumentType(Class<E> type) {
        this.type = type;
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <E> CommandArgumentType<E> of(Class<E> type, Function<String, E> parse) {
        return new CommandArgumentType<>(type) {
            @Override
            public E parse(@NotNull String arg) {
                try {
                    return parse.apply(arg);
                } catch (Exception ignore) {
                    return null;
                }
            }
        };
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <E extends Enum<E>> CommandArgumentType<E> ofEnum(Class<E> type) {
        return of(type, (string -> Enum.valueOf(type, string.toUpperCase())));
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    private static CommandArgumentType<String> string() {
        return new CommandArgumentType<>(String.class) {
            @Override
            public String parse(@NotNull String arg) {
                return arg;
            }
        };
    }

    public Class<E> getType() {
        return type;
    }

    public abstract E parse(@NotNull String arg);
}
