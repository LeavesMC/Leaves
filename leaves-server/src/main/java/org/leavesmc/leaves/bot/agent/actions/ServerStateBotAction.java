package org.leavesmc.leaves.bot.agent.actions;

import org.leavesmc.leaves.command.CommandArgument;

import java.util.function.Supplier;

public abstract class ServerStateBotAction<E extends ServerStateBotAction<E>> extends ServerBotAction<E> {

    public ServerStateBotAction(String name, CommandArgument argument, Supplier<E> creator) {
        super(name, argument, creator);
        this.setDoNumber(-1);
    }
}
