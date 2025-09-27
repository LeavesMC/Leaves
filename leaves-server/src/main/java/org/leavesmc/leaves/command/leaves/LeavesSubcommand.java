package org.leavesmc.leaves.command.leaves;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.command.LiteralNode;

public abstract class LeavesSubcommand extends LiteralNode {

    protected LeavesSubcommand(String name) {
        super(name);
    }

    @Override
    public boolean requires(@NotNull CommandSourceStack source) {
        return hasPermission(source.getSender());
    }

    protected boolean hasPermission(CommandSender sender) {
        return LeavesCommand.hasPermission(sender, this.name);
    }
}
