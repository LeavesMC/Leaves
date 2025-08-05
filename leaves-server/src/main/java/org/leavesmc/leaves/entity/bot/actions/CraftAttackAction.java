package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

public class CraftAttackAction extends CraftTimerBotAction<AttackAction, ServerAttackAction> implements AttackAction {

    public CraftAttackAction(ServerAttackAction serverAction) {
        super(serverAction, CraftAttackAction::new);
    }
}
