package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

public class CraftUseItemToOffhandAction extends CraftTimerBotAction<UseItemToOffhandAction, ServerUseItemToOffhandAction> implements UseItemToOffhandAction {

    public CraftUseItemToOffhandAction(ServerUseItemToOffhandAction serverAction) {
        super(serverAction, CraftUseItemToOffhandAction::new);
    }

    @Override
    public int getUseTick() {
        return serverAction.getUseTick();
    }

    @Override
    public CraftUseItemToOffhandAction setUseTick(int useTick) {
        serverAction.setUseTick(useTick);
        return this;
    }
}