package org.leavesmc.leaves.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class LiteralNode extends CommandNode {

    protected LiteralNode(String name) {
        super(name);
    }

    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> compileBase() {
        return Commands.literal(name);
    }
}
