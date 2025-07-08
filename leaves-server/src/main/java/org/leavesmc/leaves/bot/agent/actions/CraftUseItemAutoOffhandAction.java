package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.UseItemAutoOffhandAction;

public class CraftUseItemAutoOffhandAction extends CraftTimerBotAction<UseItemAutoOffhandAction> implements UseItemAutoOffhandAction {

    public CraftUseItemAutoOffhandAction() {
        super("use_auto_offhand", CraftUseItemAutoOffhandAction::new);
    }

    @Override
    public Class<UseItemAutoOffhandAction> getInterfaceClass() {
        return UseItemAutoOffhandAction.class;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (bot.isUsingItem()) {
            return false;
        }

        Entity entity = bot.getTargetEntity(3, null);
        BlockHitResult blockHitResult = (BlockHitResult) bot.getRayTrace(5, ClipContext.Fluid.NONE);
        if (entity != null) {
            return CraftUseItemToOffhandAction.execute(bot, entity);
        } else if (!bot.level().getBlockState(blockHitResult.getBlockPos()).isAir()) {
            return CraftUseItemOnOffhandAction.execute(bot, blockHitResult);
        } else {
            return CraftUseItemOffHandAction.execute(bot);
        }
    }
}