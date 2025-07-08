package org.leavesmc.leaves.bot.agent.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.JumpAction;

public class CraftJumpAction extends CraftTimerBotAction<JumpAction> implements JumpAction {

    public CraftJumpAction() {
        super("jump", CraftJumpAction::new);
    }

    @Override
    public Class<JumpAction> getInterfaceClass() {
        return JumpAction.class;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (bot.onGround()) {
            bot.jumpFromGround();
            return true;
        } else {
            return false;
        }
    }
}
