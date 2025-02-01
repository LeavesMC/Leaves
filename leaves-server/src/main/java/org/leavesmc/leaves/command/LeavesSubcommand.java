package org.leavesmc.leaves.command;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public interface LeavesSubcommand {
    boolean execute(CommandSender sender, String subCommand, String[] args);

    default List<String> tabComplete(final CommandSender sender, final String subCommand, final String[] args) {
        return Collections.emptyList();
    }

    default boolean tabCompletes() {
        return true;
    }
}
