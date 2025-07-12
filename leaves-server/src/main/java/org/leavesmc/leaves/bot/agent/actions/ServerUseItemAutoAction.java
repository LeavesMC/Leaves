package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.UseItemAutoAction;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemAutoAction;

public class ServerUseItemAutoAction extends ServerTimerBotAction<ServerUseItemAutoAction> {

    public ServerUseItemAutoAction() {
        super("use_auto", ServerUseItemAutoAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (bot.isUsingItem()) {
            return false;
        }

        Entity entity = bot.getTargetEntity(3, null);
        BlockHitResult blockHitResult = (BlockHitResult) bot.getRayTrace(5, ClipContext.Fluid.NONE);
        if (entity != null) {
            return ServerUseItemToAction.execute(bot, entity);
        } else if (!bot.level().getBlockState(blockHitResult.getBlockPos()).isAir()) {
            return ServerUseItemOnAction.execute(bot, blockHitResult);
        } else {
            return ServerUseItemAction.execute(bot);
        }
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemAutoAction(this);
    }
}