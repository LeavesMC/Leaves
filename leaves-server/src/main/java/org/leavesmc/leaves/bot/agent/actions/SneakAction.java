package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.BotAction;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;

public class SneakAction extends BotAction<SneakAction> {

    public SneakAction() {
        super("sneak", CommandArgument.EMPTY, SneakAction::new);
    }

    @Override
    public void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result) {
        this.setTickDelay(0).setNumber(1);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        bot.setShiftKeyDown(!bot.isShiftKeyDown());
        return true;
    }
}
