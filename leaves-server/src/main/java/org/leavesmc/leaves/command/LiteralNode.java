package org.leavesmc.leaves.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;

public class LiteralNode extends CommandNode {

    protected LiteralNode(String name) {
        super(name);
    }

    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> compileBase() {
        return Commands.literal(name);
    }

    @SuppressWarnings("unchecked")
    public void register() {
        MinecraftServer.getServer()
            .getCommands()
            .getDispatcher()
            .register((LiteralArgumentBuilder<CommandSourceStack>) compile());
    }

    public void unregister() {
        CommandDispatcher<CommandSourceStack> dispatcher = MinecraftServer.getServer()
            .getCommands()
            .getDispatcher();
        dispatcher.getRoot().removeCommand(name);
    }
}
