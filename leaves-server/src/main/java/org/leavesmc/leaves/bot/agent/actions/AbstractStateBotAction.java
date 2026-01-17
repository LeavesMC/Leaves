package org.leavesmc.leaves.bot.agent.actions;

public abstract class AbstractStateBotAction<E extends AbstractStateBotAction<E>> extends AbstractBotAction<E> {
    public AbstractStateBotAction(String name) {
        super(name);
        this.setDoNumber(-1);
    }
}
