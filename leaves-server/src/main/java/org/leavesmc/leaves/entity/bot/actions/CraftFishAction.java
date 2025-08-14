package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerFishAction;
import org.leavesmc.leaves.entity.bot.action.FishAction;

public class CraftFishAction extends CraftTimerBotAction<FishAction, ServerFishAction> implements FishAction {

    public CraftFishAction(ServerFishAction serverAction) {
        super(serverAction, CraftFishAction::new);
    }
}
