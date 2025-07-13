package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemToOffhandAction;

public class ServerUseItemToOffhandAction extends ServerTimerBotAction<ServerUseItemToOffhandAction> {

    public ServerUseItemToOffhandAction() {
        super("use_to_offhand", ServerUseItemToOffhandAction::new);
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

        boolean flag = bot.interactOn(entity, InteractionHand.OFF_HAND).consumesAction();
        if (flag) {
            bot.swing(InteractionHand.OFF_HAND);
            bot.updateItemInHand(InteractionHand.OFF_HAND);
        }
        return flag;
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemToOffhandAction(this);
    }
}
