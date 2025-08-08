package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemOnAction;

public class ServerUseItemOnAction extends ServerUseBotAction<ServerUseItemOnAction> {

    public ServerUseItemOnAction() {
        super("use_on", ServerUseItemOnAction::new);
    }

    @Override
    protected boolean interact(@NotNull ServerBot bot) {
        BlockHitResult hitResult = getBlockHitResult(bot);
        return useItemOn(bot, hitResult, InteractionHand.MAIN_HAND).consumesAction();
    }

    public static @Nullable BlockHitResult getBlockHitResult(@NotNull ServerBot bot) {
        HitResult result = bot.getRayTrace((int) bot.blockInteractionRange(), ClipContext.Fluid.NONE);
        if (result instanceof BlockHitResult blockHitResult) {
            return blockHitResult;
        } else {
            return null;
        }
    }

    public static InteractionResult useItemOn(ServerBot bot, BlockHitResult hitResult, InteractionHand hand) {
        if (hitResult == null) {
            return InteractionResult.FAIL;
        }

        BlockPos blockPos = hitResult.getBlockPos();
        if (!bot.level().getWorldBorder().isWithinBounds(blockPos)) {
            return InteractionResult.FAIL;
        }

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
