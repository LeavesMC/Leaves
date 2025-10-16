package org.leavesmc.leaves.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class LiteralNode extends CommandNode {

    protected LiteralNode(String name) {
        super(name);
    }

    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> compileBase() {
        return Commands.literal(name);
    }
}
