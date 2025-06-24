package org.leavesmc.leaves.command.subcommands;

import org.bukkit.command.CommandSender;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.LeavesSubcommand;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public class ReloadCommand implements LeavesSubcommand {
    @Override
    public void execute(CommandSender sender, String subCommand, String[] args) {
        LeavesConfig.reload();
        sender.sendMessage(text("Leaves config reload complete.", GREEN));
    }
}
