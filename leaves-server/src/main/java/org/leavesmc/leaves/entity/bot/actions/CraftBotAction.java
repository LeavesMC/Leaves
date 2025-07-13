package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.*;

public abstract class CraftBotAction {
    public abstract ServerBotAction<?> getHandle();
}
