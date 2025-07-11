package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.UseItemAutoOffhandAction;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemAutoOffhandAction;

public class ServerUseItemAutoOffhandAction extends ServerTimerBotAction<ServerUseItemAutoOffhandAction> {

    public ServerUseItemAutoOffhandAction() {
        super("use_auto_offhand", ServerUseItemAutoOffhandAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (bot.isUsingItem()) {
            return false;
        }

        Entity entity = bot.getTargetEntity(3, null);
        BlockHitResult blockHitResult = (BlockHitResult) bot.getRayTrace(5, ClipContext.Fluid.NONE);
        if (entity != null) {
            return ServerUseItemToOffhandAction.execute(bot, entity);
        } else if (!bot.level().getBlockState(blockHitResult.getBlockPos()).isAir()) {
            return ServerUseItemOnOffhandAction.execute(bot, blockHitResult);
        } else {
            return ServerUseItemOffhandAction.execute(bot);
        }
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemAutoOffhandAction(this);
    }
}