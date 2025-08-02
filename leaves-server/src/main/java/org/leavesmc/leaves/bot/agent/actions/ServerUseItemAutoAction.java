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

import java.util.Collections;

import static net.minecraft.world.InteractionResult.*;

public class ServerUseItemAutoAction extends ServerTimerBotAction<ServerUseItemAutoAction> {
    private int useTick = -1;
    private int tickToRelease = -1;
    private int useData = 0;

    public ServerUseItemAutoAction() {
        super("use_auto", CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), ServerUseItemAutoAction::new);
        this.setSuggestion(3, Pair.of(Collections.singletonList("-1"), "[UseTick]"));
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
            InteractionResult result = execute(bot);
            if (result == SUCCESS || result == SUCCESS_SERVER) {
                useData += 2;
            } else if (result == CONSUME) {
                useData += 1;
            }
            return useData >= 2;
        } else {
            bot.releaseUsingItem();
            syncTickToRelease();
            useData = 0;
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

    public InteractionResult execute(@NotNull ServerBot bot) {
        if (bot.isUsingItem()) {
            return FAIL;
        }

        EntityHitResult entityHitResult = bot.getEntityHitResult(3, null);
        BlockHitResult blockHitResult = (BlockHitResult) bot.getRayTrace(5, ClipContext.Fluid.NONE);
        boolean useTo = entityHitResult != null, useOn = !bot.level().getBlockState(blockHitResult.getBlockPos()).isAir();
        InteractionResult mainResult;
        if (useTo) {
            InteractionResult result = ServerUseItemToAction.execute(bot, entityHitResult);
            mainResult = result.consumesAction() ? result : result == InteractionResult.PASS ? ServerUseItemAction.execute(bot) : FAIL;
        } else if (useOn) {
            InteractionResult result = ServerUseItemOnAction.execute(bot, blockHitResult);
            mainResult = result.consumesAction() ? result : ServerUseItemAction.execute(bot);
        } else {
            mainResult = ServerUseItemAction.execute(bot);
        }
        if (mainResult != FAIL && mainResult != PASS) {
            return mainResult;
        }
        if (useTo) {
            InteractionResult result = ServerUseItemToOffhandAction.execute(bot, entityHitResult);
            return result.consumesAction() ? result : result == InteractionResult.PASS ? ServerUseItemOffhandAction.execute(bot) : FAIL;
        } else if (useOn) {
            InteractionResult result = ServerUseItemOnOffhandAction.execute(bot, blockHitResult);
            return result.consumesAction() ? result : ServerUseItemOffhandAction.execute(bot);
        } else {
            return ServerUseItemOffhandAction.execute(bot);
        }
    }

    @Override
    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        super.stop(bot, reason);
        bot.releaseUsingItem();
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemAutoAction(this);
    }
}