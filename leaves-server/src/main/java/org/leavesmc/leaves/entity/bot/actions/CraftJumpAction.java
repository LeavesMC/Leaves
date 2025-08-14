package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerJumpAction;
import org.leavesmc.leaves.entity.bot.action.JumpAction;

public class CraftJumpAction extends CraftTimerBotAction<JumpAction, ServerJumpAction> implements JumpAction {

    public CraftJumpAction(ServerJumpAction serverAction) {
        super(serverAction, CraftJumpAction::new);
    }
}
