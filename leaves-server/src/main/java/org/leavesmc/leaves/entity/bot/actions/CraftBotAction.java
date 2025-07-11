package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerBotAction;

public abstract class CraftBotAction {
    public abstract ServerBotAction<?> getHandle();
}
