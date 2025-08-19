package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerUseItemOnAction;
import org.leavesmc.leaves.entity.bot.action.UseItemOnAction;

public class CraftUseItemOnAction extends CraftTimerBotAction<UseItemOnAction, ServerUseItemOnAction> implements UseItemOnAction {

    public CraftUseItemOnAction(ServerUseItemOnAction serverAction) {
        super(serverAction, CraftUseItemOnAction::new);
    }

    @Override
    public int getUseTickTimeout() {
        return serverAction.getUseTickTimeout();
    }

    @Override
    public CraftUseItemOnAction setUseTickTimeout(int timeout) {
        serverAction.setUseTickTimeout(timeout);
        return this;
    }
}
