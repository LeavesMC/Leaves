package org.leavesmc.leaves.bot.agent.actions;

import java.util.function.Supplier;

public abstract class ServerStateBotAction<E extends ServerStateBotAction<E>> extends ServerBotAction<E> {

    public ServerStateBotAction(String name, Supplier<E> creator) {
        super(name, creator);
        this.setDoNumber(-1);
    }
}
