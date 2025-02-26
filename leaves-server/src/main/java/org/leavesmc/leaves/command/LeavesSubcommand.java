package org.leavesmc.leaves.command;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LeavesSubcommand {
    boolean execute(CommandSender sender, String subCommand, String[] args);

    default List<String> tabComplete(final CommandSender sender, final String subCommand, final String[] args, Location location) {
        return Collections.emptyList();
    }

    default CompletableFuture<Suggestions> tabSuggestion(final CommandSender sender, final String subCommand, final String[] args, final Location location, final SuggestionsBuilder builder) {
        return null;
    }

    default boolean tabCompletes() {
        return true;
    }
}
