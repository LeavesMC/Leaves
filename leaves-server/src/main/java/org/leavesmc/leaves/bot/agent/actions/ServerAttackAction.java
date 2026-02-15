package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftAttackAction;

public class ServerAttackAction extends AbstractTimerBotAction<ServerAttackAction> {

    public ServerAttackAction() {
        super("attack");
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        EntityHitResult hitResult = bot.getEntityHitResult(target -> target.isAttackable() && !target.skipAttackInteraction(bot));
        if (hitResult == null) {
            return false;
        } else {
            bot.attack(hitResult.getEntity());
            return true;
        }
    }

    @Override
    public Object asCraft() {
        return new CraftAttackAction(this);
    }
}
