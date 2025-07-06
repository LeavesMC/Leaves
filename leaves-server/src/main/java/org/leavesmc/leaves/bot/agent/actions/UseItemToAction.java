package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;

public class UseItemToAction extends AbstractTimerAction<UseItemToAction> {

    public UseItemToAction() {
        super("use_to", UseItemToAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Entity entity = bot.getTargetEntity(3, null);
        return execute(bot, entity);
    }

    public static boolean execute(ServerBot bot, Entity entity) {
        if (entity != null) {
            boolean flag = bot.interactOn(entity, InteractionHand.MAIN_HAND).consumesAction();
            if (flag) {
                bot.swing(InteractionHand.MAIN_HAND);
                bot.updateItemInHand(InteractionHand.MAIN_HAND);
            }
            return flag;
        }
        return false;
    }
}
