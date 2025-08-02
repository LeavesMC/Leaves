package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemOffhandAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.Collections;

import static net.minecraft.world.InteractionResult.*;

public class ServerUseItemOffhandAction extends ServerTimerBotAction<ServerUseItemOffhandAction> {
    private int useTick = -1;
    private int tickToRelease = -1;
    private byte useData = 0;

    public ServerUseItemOffhandAction() {
        super("use_offhand", CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), ServerUseItemOffhandAction::new);
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

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putInt("useTick", this.useTick);
        nbt.putInt("tickToRelease", this.tickToRelease);
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.useTick = nbt.getInt("useTick").orElseThrow();
        this.tickToRelease = nbt.getInt("tickToRelease").orElseThrow();
    }

    public int getUseTick() {
        return useTick;
    }

    public void setUseTick(int useTick) {
        this.useTick = useTick;
    }

    public static InteractionResult execute(@NotNull ServerBot bot) {
        if (bot.isUsingItem()) {
            return InteractionResult.FAIL;
        }

        InteractionResult result = bot.gameMode.useItem(bot, bot.level(), bot.getItemInHand(InteractionHand.OFF_HAND), InteractionHand.OFF_HAND);
        if (result == SUCCESS || result == SUCCESS_SERVER) {
            bot.swing(InteractionHand.OFF_HAND);
        }
        if (result.consumesAction()) {
            bot.updateItemInHand(InteractionHand.OFF_HAND);
        }
        return result;
    }
    @Override
    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        super.stop(bot, reason);
        bot.releaseUsingItem();
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemOffhandAction(this);
    }
}
