package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerBreakBlockAction;
import org.leavesmc.leaves.entity.bot.action.BreakBlockAction;

public class CraftBreakBlockAction extends CraftTimerBotAction<BreakBlockAction, ServerBreakBlockAction> implements BreakBlockAction {

    public CraftBreakBlockAction(ServerBreakBlockAction serverAction) {
        super(serverAction, CraftBreakBlockAction::new);
    }
}
