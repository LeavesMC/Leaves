package org.leavesmc.leaves.command.subcommands;

import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.LeavesSubcommand;

import java.io.File;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public class ReloadCommand implements LeavesSubcommand {
    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        MinecraftServer server = MinecraftServer.getServer();
        LeavesConfig.init((File) server.options.valueOf("leaves-settings"));
        Command.broadcastCommandMessage(sender, text("Leaves config reload complete.", GREEN));
        return false;
    }
}
