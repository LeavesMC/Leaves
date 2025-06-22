package org.leavesmc.leaves.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LeavesSuggestionCommand {
    default void suggest(@NotNull CommandSender sender, @NotNull String alias, @NotNull String @NotNull [] args, @Nullable Location location, LeavesSuggestionBuilder builder) throws IllegalArgumentException {
    }

    default boolean isEnabled() {
        return true;
    }
}