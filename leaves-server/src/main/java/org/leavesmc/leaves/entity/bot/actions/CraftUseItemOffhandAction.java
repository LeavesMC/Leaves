package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerUseItemOffhandAction;
import org.leavesmc.leaves.entity.bot.action.UseItemOffhandAction;

public class CraftUseItemOffhandAction extends CraftTimerBotAction<UseItemOffhandAction, ServerUseItemOffhandAction> implements UseItemOffhandAction {

    public CraftUseItemOffhandAction(ServerUseItemOffhandAction serverAction) {
        super(serverAction, CraftUseItemOffhandAction::new);
    }

    @Override
    public int getUseTick() {
        return serverAction.getUseTick();
    }

    @Override
    public CraftUseItemOffhandAction setUseTick(int useTick) {
        serverAction.setUseTick(useTick);
        return this;
    }
}
