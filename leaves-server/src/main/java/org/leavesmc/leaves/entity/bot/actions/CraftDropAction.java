package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerDropAction;
import org.leavesmc.leaves.entity.bot.action.DropAction;

public class CraftDropAction extends CraftTimerBotAction<DropAction, ServerDropAction> implements DropAction {

    public CraftDropAction(ServerDropAction serverAction) {
        super(serverAction, CraftDropAction::new);
    }
}
