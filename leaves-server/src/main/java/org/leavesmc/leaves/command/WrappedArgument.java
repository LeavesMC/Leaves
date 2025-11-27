package org.leavesmc.leaves.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.concurrent.CompletableFuture;

public class WrappedArgument<T> {
    private final String name;
    private final ArgumentType<T> type;
    private AsyncSuggestionProvider asyncSuggestionProvider = null;
    private SuggestionApplier suggestionApplier = null;
    private boolean optional = false;

    public WrappedArgument(String name, ArgumentType<T> type) {
        this.name = name;
        this.type = type;
    }

    public WrappedArgument<T> suggestsAsync(AsyncSuggestionProvider provider) {
        this.asyncSuggestionProvider = provider;
        return this;
    }

    public WrappedArgument<T> suggests(SuggestionApplier provider) {
        this.suggestionApplier = provider;
        return this;
    }

    public WrappedArgument<T> setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public boolean isOptional() {
        return optional;
    }

    public RequiredArgumentBuilder<CommandSourceStack, T> compile() {
        RequiredArgumentBuilder<CommandSourceStack, T> builder = Commands.argument(name, type);
        if (asyncSuggestionProvider != null) {
            builder.suggests((context, b) ->
                asyncSuggestionProvider.getSuggestions(new CommandContext(context), b)
            );
        } else if (suggestionApplier != null) {
            builder.suggests((context, b) -> {
                suggestionApplier.applySuggestions(new CommandContext(context), b);
                return CompletableFuture.completedFuture(b.build());
            });
        }
        return builder;
    }

    @FunctionalInterface
    public interface SuggestionApplier {
        void applySuggestions(final CommandContext context, final SuggestionsBuilder builder) throws CommandSyntaxException;
    }

    @FunctionalInterface
    public interface AsyncSuggestionProvider {
        CompletableFuture<Suggestions> getSuggestions(final CommandContext context, final SuggestionsBuilder builder) throws CommandSyntaxException;
    }
}
