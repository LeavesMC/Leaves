package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemToAction;

public class ServerUseItemToAction extends ServerTimerBotAction<ServerUseItemToAction> {

    public ServerUseItemToAction() {
        super("use_to", ServerUseItemToAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Entity entity = bot.getTargetEntity(3, null);
        return execute(bot, entity);
    }

    public static boolean execute(ServerBot bot, Entity entity) {
        if (entity == null) {
            return false;
        }

        boolean flag = bot.interactOn(entity, InteractionHand.MAIN_HAND).consumesAction();
        if (flag) {
            bot.swing(InteractionHand.MAIN_HAND);
            bot.updateItemInHand(InteractionHand.MAIN_HAND);
        }
        return flag;
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemToAction(this);
    }
}
