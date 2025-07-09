package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.AttackAction;

public class CraftAttackAction extends CraftTimerBotAction<AttackAction> implements AttackAction {

    public CraftAttackAction() {
        super("attack", CraftAttackAction::new);
    }

    @Override
    public @NotNull Class<AttackAction> getActionRegClass() {
        return AttackAction.class;
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
