package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.Collections;

public class ServerUseItemAction extends ServerTimerBotAction<ServerUseItemAction> {
    private int useTick = -1;
    private int tickToRelease = -1;

    public ServerUseItemAction() {
        super("use", CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), ServerUseItemAction::new);
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
            boolean result = execute(bot);
            if (useTick >=0) {
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

    public static boolean execute(@NotNull ServerBot bot) {
        if (bot.isUsingItem()) {
            return false;
        }

        boolean flag = bot.gameMode.useItem(bot, bot.level(), bot.getItemInHand(InteractionHand.MAIN_HAND), InteractionHand.MAIN_HAND).consumesAction();
        if (flag) {
            bot.swing(InteractionHand.MAIN_HAND);
            bot.updateItemInHand(InteractionHand.MAIN_HAND);
        }
        return flag;
    }

    @Override
    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        super.stop(bot, reason);
        bot.completeUsingItem();
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemAction(this);
    }
}
