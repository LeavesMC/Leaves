package org.leavesmc.leaves.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static org.leavesmc.leaves.command.CommandUtils.registerPermissions;

public abstract class RootNode extends LiteralNode {
    private final String permissionBase;

    public RootNode(String name, String permissionBase) {
        super(name);
        this.permissionBase = permissionBase;
    }

    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> compile() {
        registerPermissions(permissionBase, this.children);
        return super.compile();
    }

    protected static boolean hasPermission(@NotNull CommandSender sender, String subcommand, String permissionBase) {
        return sender.hasPermission(permissionBase) || sender.hasPermission(permissionBase + "." + subcommand);
    }

    @Override
    public boolean requires(@NotNull CommandSourceStack source) {
        return children.stream().anyMatch(child -> child.requires(source));
    }

    @SuppressWarnings("unchecked")
    public void register() {
        MinecraftServer.getServer()
            .getCommands()
            .getDispatcher()
            .register((LiteralArgumentBuilder<CommandSourceStack>) compile());
        Bukkit.getOnlinePlayers().forEach(org.bukkit.entity.Player::updateCommands);
    }

    public void unregister() {
        CommandDispatcher<CommandSourceStack> dispatcher = MinecraftServer.getServer()
            .getCommands()
            .getDispatcher();
        dispatcher.getRoot().removeCommand(name);
        Bukkit.getOnlinePlayers().forEach(org.bukkit.entity.Player::updateCommands);
    }
}
