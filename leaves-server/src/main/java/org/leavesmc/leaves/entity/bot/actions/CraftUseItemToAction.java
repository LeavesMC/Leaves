package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

public class CraftUseItemToAction extends CraftTimerBotAction<UseItemToAction, ServerUseItemToAction> implements UseItemToAction {

    public CraftUseItemToAction(ServerUseItemToAction serverAction) {
        super(serverAction, CraftUseItemToAction::new);
    }

    @Override
    public int getUseTick() {
        return serverAction.getUseTick();
    }

    @Override
    public CraftUseItemToAction setUseTick(int useTick) {
        serverAction.setUseTick(useTick);
        return this;
    }
}
