package org.leavesmc.leaves.command;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface LeavesSuggestionCommand {
    @Nullable
    CompletableFuture<Suggestions> tabSuggestion(@NotNull CommandSender sender, @NotNull String alias, @NotNull String @NotNull [] args, @Nullable Location location, @NotNull SuggestionsBuilder builder) throws IllegalArgumentException;
}
