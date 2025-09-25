package org.leavesmc.leaves.command.leaves.subcommands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.LiteralNode;
import org.leavesmc.leaves.command.leaves.LeavesSubcommand;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.spaces;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class BlockUpdateCommand extends LeavesSubcommand {
    private static boolean noBlockUpdate = false;

    public static boolean isNoBlockUpdate() {
        return LeavesConfig.modify.noBlockUpdateCommand && noBlockUpdate;
    }

    public BlockUpdateCommand() {
        super("blockupdate");
        children(
            EnableNode::new,
            DisableNode::new
        );
    }

    @Override
    protected boolean execute(@NotNull CommandContext context) throws CommandSyntaxException {
        context.getSender().sendMessage(join(spaces(),
            text("Block update is", GRAY),
            text(noBlockUpdate ? "disabled" : "enabled", AQUA)
        ));
        return true;
    }

    @Override
    public boolean requires(@NotNull CommandSourceStack source) {
        return LeavesConfig.modify.noBlockUpdateCommand && super.requires(source);
    }

    private class EnableNode extends LiteralNode {
        private EnableNode() {
            super("enable");
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) {
            CommandSender sender = context.getSender();
            if (!noBlockUpdate) {
                sender.sendMessage(join(spaces(),
                    text("Block update is already", GRAY),
                    text("enabled", AQUA)
                ));
                return true;
            }
            noBlockUpdate = false;
            Bukkit.getOnlinePlayers().stream()
                .filter(BlockUpdateCommand.this::hasPermission)
                .forEach(player -> player.sendMessage(join(spaces(),
                    text("Block update is", GRAY),
                    text("enabled", AQUA)
                )));
            return true;
        }
    }

    private class DisableNode extends LiteralNode {
        private DisableNode() {
            super("disable");
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) {
            CommandSender sender = context.getSender();
            if (noBlockUpdate) {
                sender.sendMessage(join(spaces(),
                    text("Block update is already", GRAY),
                    text("disabled", AQUA)
                ));
                return true;
            }
            noBlockUpdate = true;
            Bukkit.getOnlinePlayers().stream()
                .filter(BlockUpdateCommand.this::hasPermission)
                .forEach(player -> player.sendMessage(join(spaces(),
                    text("Block update is", GRAY),
                    text("disabled", AQUA)
                )));
            return true;
        }
    }
}
