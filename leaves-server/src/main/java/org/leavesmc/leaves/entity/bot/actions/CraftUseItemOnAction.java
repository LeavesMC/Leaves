package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerUseItemOnAction;
import org.leavesmc.leaves.entity.bot.action.UseItemOnAction;

public class CraftUseItemOnAction extends CraftTimerBotAction<UseItemOnAction, ServerUseItemOnAction> implements UseItemOnAction {

    public CraftUseItemOnAction(ServerUseItemOnAction serverAction) {
        super(serverAction, CraftUseItemOnAction::new);
    }

    @Override
    public int getUseTick() {
        return serverAction.getUseTick();
    }

    @Override
    public CraftUseItemOnAction setUseTick(int useTick) {
        serverAction.setUseTick(useTick);
        return this;
    }
}
