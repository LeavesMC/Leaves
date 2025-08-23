package org.leavesmc.leaves.neo_command.bot;

import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.neo_command.LiteralNode;

public abstract class BotSubcommand extends LiteralNode {

    protected BotSubcommand(String name) {
        super(name);
    }

    @Override
    public boolean requires(@NotNull CommandSourceStack source) {
        return BotCommand.hasPermission(source.getSender(), this.name);
    }
}
