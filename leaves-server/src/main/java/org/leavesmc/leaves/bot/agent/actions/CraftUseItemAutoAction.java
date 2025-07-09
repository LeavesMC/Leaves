package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.UseItemAutoAction;

public class CraftUseItemAutoAction extends CraftTimerBotAction<UseItemAutoAction> implements UseItemAutoAction {

    public CraftUseItemAutoAction() {
        super("use_auto", CraftUseItemAutoAction::new);
    }

    @Override
    public @NotNull Class<UseItemAutoAction> getActionRegClass() {
        return UseItemAutoAction.class;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (bot.isUsingItem()) {
            return false;
        }

        Entity entity = bot.getTargetEntity(3, null);
        BlockHitResult blockHitResult = (BlockHitResult) bot.getRayTrace(5, ClipContext.Fluid.NONE);
        if (entity != null) {
            return CraftUseItemToAction.execute(bot, entity);
        } else if (!bot.level().getBlockState(blockHitResult.getBlockPos()).isAir()) {
            return CraftUseItemOnAction.execute(bot, blockHitResult);
        } else {
            return CraftUseItemAction.execute(bot);
        }
    }
}