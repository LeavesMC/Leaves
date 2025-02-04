package org.leavesmc.leaves.command.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.util.LeavesUpdateHelper;

public class UpdateCommand implements LeavesSubcommand {

    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        sender.sendMessage(ChatColor.GRAY + "Trying to update Leaves, see the console for more info.");
        LeavesUpdateHelper.tryUpdateLeaves();
        return true;
    }

    @Override
    public boolean tabCompletes() {
        return false;
    }
}
