package org.leavesmc.leaves.bot.agent.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;

public class JumpAction extends AbstractTimerAction<JumpAction> {

    public JumpAction() {
        super("jump", JumpAction::new);
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
