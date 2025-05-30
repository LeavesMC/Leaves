package org.leavesmc.leaves.bot.agent.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;

public class DropAction extends AbstractTimerAction<DropAction> {

    public DropAction() {
        super("drop", DropAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        bot.dropAll(false);
        return true;
    }
}
