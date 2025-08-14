package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerSneakAction;
import org.leavesmc.leaves.entity.bot.action.SneakAction;

public class CraftSneakAction extends CraftBotAction<SneakAction, ServerSneakAction> implements SneakAction {

    public CraftSneakAction(ServerSneakAction serverAction) {
        super(serverAction, CraftSneakAction::new);
    }
}
