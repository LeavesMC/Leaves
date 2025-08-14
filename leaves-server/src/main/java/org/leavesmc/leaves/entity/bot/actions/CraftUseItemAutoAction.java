package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerUseItemAutoAction;
import org.leavesmc.leaves.entity.bot.action.UseItemAutoAction;

public class CraftUseItemAutoAction extends CraftTimerBotAction<UseItemAutoAction, ServerUseItemAutoAction> implements UseItemAutoAction {

    public CraftUseItemAutoAction(ServerUseItemAutoAction serverAction) {
        super(serverAction, CraftUseItemAutoAction::new);
    }

    @Override
    public int getUseTick() {
        return serverAction.getUseTick();
    }

    @Override
    public CraftUseItemAutoAction setUseTick(int useTick) {
        serverAction.setUseTick(useTick);
        return this;
    }
}
