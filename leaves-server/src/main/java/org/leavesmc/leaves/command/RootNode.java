package org.leavesmc.leaves.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.PaperCommands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static org.leavesmc.leaves.command.CommandUtils.registerPermissions;

public abstract class RootNode extends LiteralNode {

    private static final Map<String, LiteralArgumentBuilder<CommandSourceStack>> LEAVES_COMMANDS = new HashMap<>();

    public static void reloadLeavesCommands() {
        PaperCommands.INSTANCE.setValid();
        for (Map.Entry<String, LiteralArgumentBuilder<CommandSourceStack>> entry : LEAVES_COMMANDS.entrySet()) {
            PaperCommands.INSTANCE.getDispatcher().register(entry.getValue());
        }
        PaperCommands.INSTANCE.invalidate();
        Bukkit.getOnlinePlayers().forEach(org.bukkit.entity.Player::updateCommands);
    }

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
        LiteralArgumentBuilder<CommandSourceStack> builder = (LiteralArgumentBuilder<CommandSourceStack>) compile();
        LEAVES_COMMANDS.put(name, builder);
        PaperCommands.INSTANCE.setValid();
        PaperCommands.INSTANCE.getDispatcher().register(builder);
        PaperCommands.INSTANCE.invalidate();
        Bukkit.getOnlinePlayers().forEach(org.bukkit.entity.Player::updateCommands);
    }

    public void unregister() {
        PaperCommands.INSTANCE.setValid();
        PaperCommands.INSTANCE.getDispatcher().getRoot().removeCommand(name);
        PaperCommands.INSTANCE.invalidate();
        LEAVES_COMMANDS.remove(name);
        Bukkit.getOnlinePlayers().forEach(org.bukkit.entity.Player::updateCommands);
    }
}
