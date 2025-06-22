package org.leavesmc.leaves.command;

import org.bukkit.command.CommandSender;

public interface LeavesSubcommand extends LeavesSuggestionCommand {
    void execute(CommandSender sender, String subCommand, String[] args);
}