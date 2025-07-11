package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.UseItemOffHandAction;

public class CraftUseItemOffHandAction extends ServerTimerBotAction<UseItemOffHandAction> implements UseItemOffHandAction {

    public CraftUseItemOffHandAction() {
        super("use_offhand", CraftUseItemOffHandAction::new);
    }

    @Override
    public @NotNull Class<UseItemOffHandAction> getActionRegClass() {
        return UseItemOffHandAction.class;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return execute(bot);
    }

    public static boolean execute(@NotNull ServerBot bot) {
        if (bot.isUsingItem()) {
            return false;
        }

        boolean flag = bot.gameMode.useItem(bot, bot.level(), bot.getItemInHand(InteractionHand.OFF_HAND), InteractionHand.OFF_HAND).consumesAction();
        if (flag) {
            bot.swing(InteractionHand.OFF_HAND);
            bot.updateItemInHand(InteractionHand.OFF_HAND);
        }
        return flag;
    }
}
