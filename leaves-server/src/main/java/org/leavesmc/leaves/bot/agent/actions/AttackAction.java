package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;

public class AttackAction extends AbstractTimerAction<AttackAction> {

    public AttackAction() {
        super("attack", AttackAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Entity entity = bot.getTargetEntity(3, target -> target.isAttackable() && !target.skipAttackInteraction(bot));
        if (entity != null) {
            bot.attack(entity);
            return true;
        }
        return false;
    }
}
