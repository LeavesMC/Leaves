package org.leavesmc.leaves.command;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;

public class LeavesSuggestionBuilder {

    private SuggestionsBuilder vanillaBuilder;

    public LeavesSuggestionBuilder(SuggestionsBuilder builder) {
        this.vanillaBuilder = builder;
    }

    public CompletableFuture<Suggestions> build() {
        return vanillaBuilder.buildFuture();
    }

    public LeavesSuggestionBuilder suggest(String text) {
        vanillaBuilder.suggest(text);
        return this;
    }

    public LeavesSuggestionBuilder suggest(String text, Message tooltip) {
        vanillaBuilder.suggest(text, tooltip);
        return this;
    }

    public LeavesSuggestionBuilder suggest(int value) {
        vanillaBuilder.suggest(value);
        return this;
    }

    public LeavesSuggestionBuilder suggest(int value, Message tooltip) {
        vanillaBuilder.suggest(value, tooltip);
        return this;
    }

    public LeavesSuggestionBuilder createOffset(int start) {
        vanillaBuilder = vanillaBuilder.createOffset(start);
        return this;
    }

    public String getInput() {
        return vanillaBuilder.getInput();
    }
}
