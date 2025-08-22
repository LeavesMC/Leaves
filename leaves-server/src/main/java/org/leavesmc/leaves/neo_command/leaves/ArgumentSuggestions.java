package org.leavesmc.leaves.neo_command.leaves;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.neo_command.WrappedArgument;

import java.util.List;

public class ArgumentSuggestions {
    @Contract(pure = true)
    public static WrappedArgument.@NotNull SuggestionApplier strings(String... values) {
        return (context, builder) -> {
            for (String s : values) {
                builder.suggest(s);
            }
        };
    }

    @Contract(pure = true)
    public static WrappedArgument.@NotNull SuggestionApplier strings(List<String> values) {
        return (context, builder) -> {
            for (String s : values) {
                builder.suggest(s);
            }
        };
    }
}
