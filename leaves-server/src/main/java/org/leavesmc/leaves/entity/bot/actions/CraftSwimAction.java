package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerSwimAction;
import org.leavesmc.leaves.entity.bot.action.SwimAction;

public class CraftSwimAction extends CraftBotAction<SwimAction, ServerSwimAction> implements SwimAction {

    public CraftSwimAction(ServerSwimAction serverAction) {
        super(serverAction, CraftSwimAction::new);
    }
}
