package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.actions.CraftSwapAction;

public class ServerSwapAction extends ServerBotAction<ServerSwapAction> {

    public ServerSwapAction() {
        super("swap", CommandArgument.EMPTY, ServerSwapAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        ItemStack mainHandItem = bot.getMainHandItem();
        ItemStack offHandItem = bot.getOffhandItem();
        bot.setItemInHand(InteractionHand.MAIN_HAND, offHandItem);
        bot.setItemInHand(InteractionHand.OFF_HAND, mainHandItem);
        return true;
    }

    @Override
    public Object asCraft() {
        return new CraftSwapAction(this);
    }
}
