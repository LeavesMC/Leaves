package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.UseItemAction;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemAction;

public class ServerUseItemAction extends ServerTimerBotAction<ServerUseItemAction> {

    public ServerUseItemAction() {
        super("use", ServerUseItemAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return execute(bot);
    }

    public static boolean execute(@NotNull ServerBot bot) {
        if (bot.isUsingItem()) {
            return false;
        }

        boolean flag = bot.gameMode.useItem(bot, bot.level(), bot.getItemInHand(InteractionHand.MAIN_HAND), InteractionHand.MAIN_HAND).consumesAction();
        if (flag) {
            bot.swing(InteractionHand.MAIN_HAND);
            bot.updateItemInHand(InteractionHand.MAIN_HAND);
        }
        return flag;
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemAction(this);
    }
}
