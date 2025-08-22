package org.leavesmc.leaves.neo_command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

public class LiteralNode extends CommandNode {

    protected LiteralNode(String name) {
        super(name);
    }

    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> compileBase() {
        return Commands.literal(name);
    }

    @SuppressWarnings("unchecked")
    public void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder<CommandSourceStack>) compile());
    }
}
