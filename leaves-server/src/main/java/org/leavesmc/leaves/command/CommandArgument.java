package org.leavesmc.leaves.command;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class CommandArgument {

    public static final CommandArgument EMPTY = new CommandArgument();

    private static final Pair<List<String>, String> EMPTY_SUGGESTION_RESULT = Pair.of(List.of(), null);
    private static final BiFunction<CommandSender, String, Pair<List<String>, String>> EMPTY_SUGGESTION = (sender, arg) -> EMPTY_SUGGESTION_RESULT;

    private final List<BiFunction<CommandSender, String, Pair<List<String>, String>>> suggestions;
    private final List<CommandArgumentType<?>> argumentTypes;

    private CommandArgument(CommandArgumentType<?>... argumentTypes) {
        this.argumentTypes = List.of(argumentTypes);
        this.suggestions = new ArrayList<>();
        for (int i = 0; i < argumentTypes.length; i++) {
            suggestions.add(EMPTY_SUGGESTION);
        }
    }

    public static CommandArgument of(CommandArgumentType<?>... argumentTypes) {
        return new CommandArgument(argumentTypes);
    }

    public List<CommandArgumentType<?>> getArgumentTypes() {
        return argumentTypes;
    }

    public CommandArgument setSuggestion(int n, BiFunction<CommandSender, String, Pair<List<String>, String>> suggestion) {
        this.suggestions.set(n, suggestion);
        return this;
    }

    public CommandArgument setSuggestion(int n, Pair<List<String>, String> suggestion) {
        return this.setSuggestion(n, (sender, arg) -> suggestion);
    }

    public CommandArgument setSuggestion(int n, List<String> tabComplete) {
        return this.setSuggestion(n, Pair.of(tabComplete, null));
    }

    public Pair<List<String>, String> suggestion(int n, CommandSender sender, String arg) {
        if (suggestions.size() > n) {
            return suggestions.get(n).apply(sender, arg);
        } else {
            return EMPTY_SUGGESTION.apply(sender, arg);
        }
    }

    public CommandArgumentResult parse(int index, String @NotNull [] args) {
        Object[] result = new Object[argumentTypes.size()];
        Arrays.fill(result, null);
        for (int i = index, j = 0; i < args.length && j < result.length; i++, j++) {
            result[j] = argumentTypes.get(j).parse(args[i]);
        }
        return new CommandArgumentResult(new ArrayList<>(Arrays.asList(result)));
    }
}
