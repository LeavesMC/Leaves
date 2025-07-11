package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.action.SwimAction;

public class CraftSwimAction extends ServerStateBotAction<SwimAction> implements SwimAction {

    public CraftSwimAction() {
        super("swim", CommandArgument.EMPTY, CraftSwimAction::new);
    }

    @Override
    public @NotNull Class<SwimAction> getActionRegClass() {
        return SwimAction.class;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (bot.isInWater()) {
            bot.addDeltaMovement(new Vec3(0, 0.03, 0));
        }
        return true;
    }
}
