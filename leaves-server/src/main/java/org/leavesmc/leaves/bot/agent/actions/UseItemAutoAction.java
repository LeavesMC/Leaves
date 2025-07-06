package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;

public class UseItemAutoAction extends AbstractTimerAction<UseItemAutoAction> {

    public UseItemAutoAction() {
        super("use_auto", UseItemAutoAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (bot.isUsingItem()) {
            return false;
        }

        Entity entity = bot.getTargetEntity(3, null);
        BlockHitResult blockHitResult = (BlockHitResult) bot.getRayTrace(5, ClipContext.Fluid.NONE);
        if (entity != null) {
            return UseItemToAction.execute(bot, entity);
        } else if (!bot.level().getBlockState(blockHitResult.getBlockPos()).isAir()) {
            return UseItemOnAction.execute(bot, blockHitResult);
        } else {
            return UseItemAction.execute(bot);
        }
    }
}