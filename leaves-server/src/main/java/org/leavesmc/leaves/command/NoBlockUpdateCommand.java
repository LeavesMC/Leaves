package org.leavesmc.leaves.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;

import java.util.List;

public class NoBlockUpdateCommand extends Command {

    private static boolean noBlockUpdate = false;

    public NoBlockUpdateCommand(@NotNull String name) {
        super(name);
        this.description = "No Block Update Command";
        this.usageMessage = "/blockupdate";
        this.setPermission("bukkit.command.blockupdate");
        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        if (pluginManager.getPermission("bukkit.command.blockupdate") == null) {
            pluginManager.addPermission(new Permission("bukkit.command.blockupdate", PermissionDefault.OP));
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String @NotNull [] args) throws IllegalArgumentException {
        return List.of();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String @NotNull [] args) {
        if (!testPermission(sender)) return true;
        noBlockUpdate = !noBlockUpdate;
        Bukkit.broadcast(Component.join(JoinConfiguration.noSeparators(),
            Component.text("Block update status: ", NamedTextColor.GRAY),
            Component.text(!noBlockUpdate, noBlockUpdate ? NamedTextColor.AQUA : NamedTextColor.GRAY)
        ), "bukkit.command.blockupdate");

        return true;
    }

    public static void setPreventBlockUpdate(boolean preventBlockUpdate) {
        noBlockUpdate = !preventBlockUpdate;
    }

    public static boolean isNoBlockUpdate() {
        return LeavesConfig.modify.noBlockUpdateCommand && noBlockUpdate;
    }
}
