package org.leavesmc.leaves.bot.agent.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.action.SneakAction;
import org.leavesmc.leaves.entity.bot.actions.CraftSneakAction;

public class ServerSneakAction extends ServerBotAction<ServerSneakAction> {

    public ServerSneakAction() {
        super("sneak", CommandArgument.EMPTY, ServerSneakAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        bot.setShiftKeyDown(!bot.isShiftKeyDown());
        return true;
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
