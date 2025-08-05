package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

public class CraftSwapAction extends CraftBotAction<SwapAction, ServerSwapAction> implements SwapAction {

    public CraftSwapAction(ServerSwapAction serverAction) {
        super(serverAction, CraftSwapAction::new);
    }
}
