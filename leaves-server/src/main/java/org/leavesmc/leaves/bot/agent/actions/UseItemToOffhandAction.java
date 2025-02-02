package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;

public class UseItemToOffhandAction extends AbstractTimerAction<UseItemToOffhandAction> {

    public UseItemToOffhandAction() {
        super("use_to_offhand", UseItemToOffhandAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Entity entity = bot.getTargetEntity(3, null);
        if (entity != null) {
            boolean flag = bot.interactOn(entity, InteractionHand.OFF_HAND).consumesAction();
            if (flag) {
                bot.swing(InteractionHand.OFF_HAND);
                bot.updateItemInHand(InteractionHand.OFF_HAND);
            }
            return flag;
        }
        return false;
    }
}
