package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.UseItemToAction;

public class CraftUseItemToAction extends ServerTimerBotAction<UseItemToAction> implements UseItemToAction {

    public CraftUseItemToAction() {
        super("use_to", CraftUseItemToAction::new);
    }

    @Override
    public @NotNull Class<UseItemToAction> getActionRegClass() {
        return UseItemToAction.class;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Entity entity = bot.getTargetEntity(3, null);
        return execute(bot, entity);
    }

    public static boolean execute(ServerBot bot, Entity entity) {
        if (entity == null) return false;
        boolean flag = bot.interactOn(entity, InteractionHand.MAIN_HAND).consumesAction();
        if (flag) {
            bot.swing(InteractionHand.MAIN_HAND);
            bot.updateItemInHand(InteractionHand.MAIN_HAND);
        }
        return flag;
    }
}
