package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerUseItemAutoAction;
import org.leavesmc.leaves.entity.bot.action.UseItemAutoAction;

public class CraftUseItemAutoAction extends CraftTimerBotAction<UseItemAutoAction, ServerUseItemAutoAction> implements UseItemAutoAction {

    public CraftUseItemAutoAction(ServerUseItemAutoAction serverAction) {
        super(serverAction, CraftUseItemAutoAction::new);
    }

    @Override
    public int getUseTickTimeout() {
        return serverAction.getUseTickTimeout();
    }

    @Override
    public CraftUseItemAutoAction setUseTickTimeout(int timeout) {
        serverAction.setUseTickTimeout(timeout);
        return this;
    }
}
