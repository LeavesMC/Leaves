package org.leavesmc.leaves.bot.agent.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.action.SneakAction;

public class CraftSneakAction extends ServerBotAction<SneakAction> implements SneakAction {

    public CraftSneakAction() {
        super("sneak", CommandArgument.EMPTY, CraftSneakAction::new);
    }

    @Override
    public @NotNull Class<SneakAction> getActionRegClass() {
        return SneakAction.class;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        bot.setShiftKeyDown(!bot.isShiftKeyDown());
        return true;
    }
}
