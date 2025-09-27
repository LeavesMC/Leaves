package org.leavesmc.leaves.command.leaves.subcommands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.leaves.LeavesSubcommand;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public class ReloadCommand extends LeavesSubcommand {
    public ReloadCommand() {
        super("reload");
    }

    @Override
    protected boolean execute(@NotNull CommandContext context) throws CommandSyntaxException {
        LeavesConfig.reload();
        CommandSender sender = context.getSender();
        sender.sendMessage(text("Leaves config reload complete", GREEN));
        Bukkit.getOnlinePlayers().stream()
            .filter(player -> player.hasPermission("leaves.command.config.notify") && player != sender)
            .forEach(player -> player.sendMessage(text("Leaves config reloaded", GREEN)));
        return true;
    }
}
