package org.leavesmc.leaves.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ArgumentNode<T> extends CommandNode {
    protected final ArgumentType<T> argumentType;

    protected ArgumentNode(String name, ArgumentType<T> argumentType) {
        super(name);
        this.argumentType = argumentType;
    }

    @SuppressWarnings({"unused", "RedundantThrows"})
    protected CompletableFuture<Suggestions> getSuggestions(final CommandContext context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        return Suggestions.empty();
    }

    protected boolean overrideSuggestions() {
        return isMethodOverridden("getSuggestions", ArgumentNode.class);
    }

    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> compileBase() {
        RequiredArgumentBuilder<CommandSourceStack, T> argumentBuilder = Commands.argument(name, argumentType);

        if (overrideSuggestions()) {
            argumentBuilder.suggests(
                (context, builder) -> getSuggestions(new CommandContext(context), builder)
            );
        }

        return argumentBuilder;
    }

    public static class ArgumentSuggestions {
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
}
