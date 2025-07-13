package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
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
        boolean mainSuccess, useTo = entity != null, useOn = !bot.level().getBlockState(blockHitResult.getBlockPos()).isAir();
        if (useTo) {
            mainSuccess = ServerUseItemToAction.execute(bot, entity) || ServerUseItemAction.execute(bot);
        } else if (useOn) {
            mainSuccess = ServerUseItemOnAction.execute(bot, blockHitResult) || ServerUseItemAction.execute(bot);
        } else {
            mainSuccess = ServerUseItemAction.execute(bot);
        }
        if (mainSuccess) {
            return true;
        }
        if (useTo) {
            return ServerUseItemToOffhandAction.execute(bot, entity) || ServerUseItemOffhandAction.execute(bot);
        } else if (useOn) {
            return ServerUseItemOnOffhandAction.execute(bot, blockHitResult) || ServerUseItemOffhandAction.execute(bot);
        } else {
            return ServerUseItemOffhandAction.execute(bot);
        }
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemAutoAction(this);
    }
}