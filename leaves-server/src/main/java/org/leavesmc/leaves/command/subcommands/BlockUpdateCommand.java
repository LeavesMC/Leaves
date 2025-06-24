package org.leavesmc.leaves.command.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.LeavesSubcommand;

public class BlockUpdateCommand implements LeavesSubcommand {

    private static boolean noBlockUpdate = false;

    public static boolean isNoBlockUpdate() {
        return LeavesConfig.modify.noBlockUpdateCommand && noBlockUpdate;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String commandLabel, String @NotNull [] args) {
        noBlockUpdate = !noBlockUpdate;
        Bukkit.broadcast(Component.join(JoinConfiguration.noSeparators(),
            Component.text("Block update status: ", NamedTextColor.GRAY),
            Component.text(!noBlockUpdate, noBlockUpdate ? NamedTextColor.AQUA : NamedTextColor.GRAY)
        ), "bukkit.command.leaves.blockupdate");
    }

    @Override
    public boolean isEnabled() {
        return LeavesConfig.modify.noBlockUpdateCommand;
    }
}
