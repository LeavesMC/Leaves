package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerUseItemOffhandAction;
import org.leavesmc.leaves.entity.bot.action.UseItemOffhandAction;

public class CraftUseItemOffhandAction extends CraftTimerBotAction<UseItemOffhandAction, ServerUseItemOffhandAction> implements UseItemOffhandAction {

    public CraftUseItemOffhandAction(ServerUseItemOffhandAction serverAction) {
        super(serverAction, CraftUseItemOffhandAction::new);
    }

    @Override
    public int getUseTickTimeout() {
        return serverAction.getUseTickTimeout();
    }

    @Override
    public CraftUseItemOffhandAction setUseTickTimeout(int timeout) {
        serverAction.setUseTickTimeout(timeout);
        return this;
    }
}
