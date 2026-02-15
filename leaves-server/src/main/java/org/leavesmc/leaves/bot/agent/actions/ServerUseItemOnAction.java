package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemOnAction;

public class ServerUseItemOnAction extends AbstractUseBotAction<ServerUseItemOnAction> {

    public ServerUseItemOnAction() {
        super("use_on");
    }

    @Override
    protected boolean interact(@NotNull ServerBot bot) {
        BlockHitResult hitResult = bot.getBlockHitResult();
        return useItemOn(bot, hitResult, InteractionHand.MAIN_HAND).consumesAction();
    }

    public static InteractionResult useItemOn(ServerBot bot, BlockHitResult hitResult, InteractionHand hand) {
        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResult.FAIL;
        }

        BlockPos blockPos = hitResult.getBlockPos();
        if (!bot.level().getWorldBorder().isWithinBounds(blockPos)) {
            return InteractionResult.FAIL;
        }

        bot.updateItemInHand(hand);
        InteractionResult interactionResult = bot.gameMode.useItemOn(bot, bot.level(), bot.getItemInHand(hand), hand, hitResult);
        if (shouldSwing(interactionResult)) {
            bot.swing(hand);
        }

        return interactionResult;
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemOnAction(this);
    }
}
