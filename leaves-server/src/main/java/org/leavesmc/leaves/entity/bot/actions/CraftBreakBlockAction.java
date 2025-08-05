package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

public class CraftBreakBlockAction extends CraftTimerBotAction<BreakBlockAction, ServerBreakBlockAction> implements BreakBlockAction {

    public CraftBreakBlockAction(ServerBreakBlockAction serverAction) {
        super(serverAction, CraftBreakBlockAction::new);
    }
}
