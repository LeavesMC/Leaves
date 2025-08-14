package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerUseItemOnOffhandAction;
import org.leavesmc.leaves.entity.bot.action.UseItemOnOffhandAction;

public class CraftUseItemOnOffhandAction extends CraftTimerBotAction<UseItemOnOffhandAction, ServerUseItemOnOffhandAction> implements UseItemOnOffhandAction {

    public CraftUseItemOnOffhandAction(ServerUseItemOnOffhandAction serverAction) {
        super(serverAction, CraftUseItemOnOffhandAction::new);
    }

    @Override
    public int getUseTick() {
        return serverAction.getUseTick();
    }

    @Override
    public CraftUseItemOnOffhandAction setUseTick(int useTick) {
        serverAction.setUseTick(useTick);
        return this;
    }
}
