package org.leavesmc.leaves.bot.agent.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.action.SneakAction;
import org.leavesmc.leaves.entity.bot.actions.CraftSneakAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

public class ServerSneakAction extends ServerStateBotAction<ServerSneakAction> {

    public ServerSneakAction() {
        super("sneak", CommandArgument.EMPTY, ServerSneakAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (bot.isShiftKeyDown()) return false;

        bot.setShiftKeyDown(true);
        return true;
    }

    @Override
    public void onStop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        bot.setShiftKeyDown(false);
    }

    @Override
    public @NotNull Class<SneakAction> getActionClass() {
        return SneakAction.class;
    }

    @Override
    public Object asCraft() {
        return new CraftSneakAction(this);
    }
}
