package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.actions.CraftSwimAction;

public class ServerSwimAction extends ServerStateBotAction<ServerSwimAction> {

    public ServerSwimAction() {
        super("swim", CommandArgument.EMPTY, ServerSwimAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (bot.isInWater()) {
            bot.addDeltaMovement(new Vec3(0, 0.03, 0));
        }
        return true;
    }

    @Override
    public Object asCraft() {
        return new CraftSwimAction(this);
    }
}
