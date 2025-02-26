package org.leavesmc.leaves.command.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.util.LeavesUpdateHelper;

public class UpdateCommand implements LeavesSubcommand {

    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        sender.sendMessage(Component.text("Trying to update Leaves, see the console for more info.", NamedTextColor.GRAY));
        LeavesUpdateHelper.tryUpdateLeaves();
        return true;
    }

    @Override
    public boolean tabCompletes() {
        return false;
    }
}
