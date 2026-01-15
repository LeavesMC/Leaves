package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemToOffhandAction;

import static org.leavesmc.leaves.bot.agent.actions.ServerUseItemToAction.useItemTo;

public class ServerUseItemToOffhandAction extends AbstractUseBotAction<ServerUseItemToOffhandAction> {

    public ServerUseItemToOffhandAction() {
        super("use_to_offhand");
    }

    @Override
    protected boolean interact(@NotNull ServerBot bot) {
        EntityHitResult hitResult = bot.getEntityHitResult();
        return useItemTo(bot, hitResult, InteractionHand.OFF_HAND).consumesAction();
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemToOffhandAction(this);
    }
}
