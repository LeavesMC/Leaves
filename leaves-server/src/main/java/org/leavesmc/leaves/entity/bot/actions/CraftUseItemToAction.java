package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerUseItemToAction;
import org.leavesmc.leaves.entity.bot.action.UseItemToAction;

public class CraftUseItemToAction extends CraftTimerBotAction<UseItemToAction, ServerUseItemToAction> implements UseItemToAction {

    public CraftUseItemToAction(ServerUseItemToAction serverAction) {
        super(serverAction, CraftUseItemToAction::new);
    }

    @Override
    public int getUseTickTimeout() {
        return serverAction.getUseTickTimeout();
    }

    @Override
    public CraftUseItemToAction setUseTickTimeout(int timeout) {
        serverAction.setUseTickTimeout(timeout);
        return this;
    }
}
