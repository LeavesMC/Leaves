package org.leavesmc.leaves.bot.agent.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftJumpAction;

public class ServerJumpAction extends AbstractTimerBotAction<ServerJumpAction> {

    public ServerJumpAction() {
        super("jump");
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (!bot.onGround()) {
            return false;
        } else {
            bot.jumpFromGround();
        }
        return true;
    }

    @Override
    public Object asCraft() {
        return new CraftJumpAction(this);
    }
}
