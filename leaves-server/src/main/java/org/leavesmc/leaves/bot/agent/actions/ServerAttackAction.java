package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.AttackAction;
import org.leavesmc.leaves.entity.bot.actions.CraftAttackAction;

public class ServerAttackAction extends ServerTimerBotAction<ServerAttackAction> {

    public ServerAttackAction() {
        super("attack", ServerAttackAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Entity entity = bot.getTargetEntity(3, target -> target.isAttackable() && !target.skipAttackInteraction(bot));
        if (entity == null) {
            return false;
        } else {
            bot.attack(entity);
            return true;
        }
    }

    @Override
    public Object asCraft() {
        return new CraftAttackAction(this);
    }
}
