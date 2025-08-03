package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemAutoAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.List;

public class ServerUseItemAutoAction extends ServerTimerBotAction<ServerUseItemAutoAction> {
    private int useTick = -1;
    private int tickToRelease = -1;

    public ServerUseItemAutoAction() {
        super("use_auto", CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), ServerUseItemAutoAction::new);
        this.setSuggestion(3, Pair.of(List.of("-1"), "[UseTick]"));
    }

    @Override
    public void init() {
        super.init();
        syncTickToRelease();
    }

    @Override
    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) {
        super.loadCommand(player, result);
        this.useTick = result.readInt(-1);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        tickToRelease--;
        if (tickToRelease >= 0) {
            boolean result = execute(bot);
            if (useTick >= 0) {
                return false;
            } else {
                return result;
            }
        } else {
            syncTickToRelease();
            bot.releaseUsingItem();
            return true;
        }
    }

    private void syncTickToRelease() {
        if (this.useTick >= 0) {
            this.tickToRelease = this.useTick;
        } else {
            this.tickToRelease = Integer.MAX_VALUE;
        }
    }

    public int getUseTick() {
        return useTick;
    }

    public void setUseTick(int useTick) {
        this.useTick = useTick;
    }

    public boolean execute(@NotNull ServerBot bot) {
        if (bot.isUsingItem()) {
            return false;
        }

        EntityHitResult entityHitResult = bot.getEntityHitResult(3, null);
        BlockHitResult blockHitResult = (BlockHitResult) bot.getRayTrace(5, ClipContext.Fluid.NONE);
        boolean mainSuccess, useTo = entityHitResult != null, useOn = !bot.level().getBlockState(blockHitResult.getBlockPos()).isAir();
        if (useTo) {
            InteractionResult result = ServerUseItemToAction.execute(bot, entityHitResult);
            mainSuccess = result.consumesAction() || (result == InteractionResult.PASS && ServerUseItemAction.execute(bot));
        } else if (useOn) {
            mainSuccess = ServerUseItemOnAction.execute(bot, blockHitResult) || ServerUseItemAction.execute(bot);
        } else {
            mainSuccess = ServerUseItemAction.execute(bot);
        }
        if (mainSuccess) {
            return true;
        }
        if (useTo) {
            InteractionResult result = ServerUseItemToOffhandAction.execute(bot, entityHitResult);
            return result.consumesAction() || (result == InteractionResult.PASS && ServerUseItemOffhandAction.execute(bot));
        } else if (useOn) {
            return ServerUseItemOnOffhandAction.execute(bot, blockHitResult) || ServerUseItemOffhandAction.execute(bot);
        } else {
            return ServerUseItemOffhandAction.execute(bot);
        }
    }

    @Override
    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        super.stop(bot, reason);
        bot.completeUsingItem();
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemAutoAction(this);
    }
}