package org.leavesmc.leaves.bot.agent.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftSneakAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

public class ServerSneakAction extends AbstractStateBotAction<ServerSneakAction> {

    public ServerSneakAction() {
        super("sneak");
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (bot.isShiftKeyDown()) {
            return false;
        }

        bot.setShiftKeyDown(true);
        return true;
    }

    @Override
    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        super.stop(bot, reason);
        bot.setShiftKeyDown(false);
    }

    @Override
    public Object asCraft() {
        return new CraftSneakAction(this);
    }
}
