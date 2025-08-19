package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerUseItemOnOffhandAction;
import org.leavesmc.leaves.entity.bot.action.UseItemOnOffhandAction;

public class CraftUseItemOnOffhandAction extends CraftTimerBotAction<UseItemOnOffhandAction, ServerUseItemOnOffhandAction> implements UseItemOnOffhandAction {

    public CraftUseItemOnOffhandAction(ServerUseItemOnOffhandAction serverAction) {
        super(serverAction, CraftUseItemOnOffhandAction::new);
    }

    @Override
    public int getUseTickTimeout() {
        return serverAction.getUseTickTimeout();
    }

    @Override
    public CraftUseItemOnOffhandAction setUseTickTimeout(int timeout) {
        serverAction.setUseTickTimeout(timeout);
        return this;
    }
}
