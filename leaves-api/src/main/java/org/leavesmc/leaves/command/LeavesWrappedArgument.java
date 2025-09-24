package org.leavesmc.leaves.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public interface LeavesWrappedArgument<T> {

    LeavesWrappedArgument<T> suggestsAsync(AsyncSuggestionProvider provider);

    LeavesWrappedArgument<T> suggests(SuggestionApplier provider);

    LeavesWrappedArgument<T> setOptional(boolean optional);

    boolean isOptional();

    @FunctionalInterface
    interface SuggestionApplier {
        void applySuggestions(final LeavesCommandContext context, final SuggestionsBuilder builder) throws CommandSyntaxException;
    }

    @FunctionalInterface
    interface AsyncSuggestionProvider {
        CompletableFuture<Suggestions> getSuggestions(final LeavesCommandContext context, final SuggestionsBuilder builder) throws CommandSyntaxException;
    }

    interface ArgumentHandler {
        <T> LeavesWrappedArgument<T> create(String name, com.mojang.brigadier.arguments.ArgumentType<T> type);

        void fork(int forkId);
    }
}
