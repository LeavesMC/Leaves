package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgumentResult;

public class DropAction extends AbstractTimerAction<DropAction> {

    public DropAction() {
        super("drop", DropAction::new);
    }

    @Override
    public void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result) {
        this.setInitialTickDelay(result.readInt(100)).setInitialTickInterval(result.readInt(100)).setInitialNumber(result.readInt(1));
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        bot.dropAll();
        return true;
    }
}
