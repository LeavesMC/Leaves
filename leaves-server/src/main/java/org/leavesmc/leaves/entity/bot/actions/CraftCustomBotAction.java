package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerCustomBotAction;
import org.leavesmc.leaves.entity.bot.action.CustomBotAction;

public class CraftCustomBotAction extends CraftBotAction<CustomBotAction.InternalCustomBotAction, ServerCustomBotAction> implements CustomBotAction.InternalCustomBotAction {

    public CraftCustomBotAction(ServerCustomBotAction serverAction) {
        super(serverAction, CraftCustomBotAction::new);
    }
}
