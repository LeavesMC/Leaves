package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.AbstractBotAction;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;

public class SwimAction extends AbstractBotAction<SwimAction> {

    public SwimAction() {
        super("swim", CommandArgument.EMPTY, SwimAction::new);
    }

    @Override
    public void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result) {
        this.setInitialTickDelay(0).setInitialTickInterval(1).setInitialNumber(-1);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (bot.isInWater()) {
            bot.addDeltaMovement(new Vec3(0, 0.03, 0));
        }
        return true;
    }
}
