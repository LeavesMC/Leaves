package org.leavesmc.leaves.bot.agent.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.DropAction;

public class CraftDropAction extends CraftTimerBotAction<DropAction> implements DropAction {

    public CraftDropAction() {
        super("drop", CraftDropAction::new);
    }

    @Override
    public @NotNull Class<DropAction> getActionRegClass() {
        return DropAction.class;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        bot.dropAll(false);
        return true;
    }
}
