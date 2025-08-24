package org.leavesmc.leaves.bot.agent.actions;

import java.util.function.Supplier;

public abstract class AbstractStateBotAction<E extends AbstractStateBotAction<E>> extends AbstractBotAction<E> {

    public AbstractStateBotAction(String name, Supplier<E> creator) {
        super(name, creator);
        this.setDoNumber(-1);
    }
}
