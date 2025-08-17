package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.List;
import java.util.function.Supplier;

public abstract class ServerUseBotAction<T extends ServerUseBotAction<T>> extends ServerTimerBotAction<T> {
    private int useTickTimeout = -1;
    private int alreadyUsedTick = 0;
    private int useItemRemainingTicks = 0;

    public ServerUseBotAction(String name, Supplier<T> supplier) {
        super(name, CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), supplier);
        this.setSuggestion(3, Pair.of(List.of("-1"), "[UseTickTimeout]"));
    }

    @Override
    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) {
        super.loadCommand(player, result);
        this.useTickTimeout = result.readInt(-1);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (shouldStartUseItem()) {
            boolean isSuccess = interact(bot);
            syncUseItemRemainingTicks(bot);
            if (alreadyUseOver()) {
                resetAlreadyUsedTick();
                return isSuccess;
            }
        } else {
            syncUseItemRemainingTicks(bot);
        }

        if (alreadyUseOver()) {
            resetAlreadyUsedTick();
            bot.completeUsingItem();
            return true;
        } else {
            increaseAlreadyUsedTick();
            if (isUseTickLimitExceeded()) {
                resetAlreadyUsedTick();
                shouldStartUseItemNextTick();
                return bot.releaseUsingItemWithResult();
            }
            return false;
        }
    }

    protected abstract boolean interact(ServerBot bot);

    public static boolean shouldSwing(InteractionResult result) {
        return result instanceof InteractionResult.Success success && success.swingSource() != InteractionResult.SwingSource.NONE;
    }

    private boolean shouldStartUseItem() {
        return useItemRemainingTicks == 0;
    }

    private boolean alreadyUseOver() {
        return useItemRemainingTicks == 0;
    }

    private boolean isUseTickLimitExceeded() {
        int useTickLimit = useTickTimeout == -1 ? Integer.MAX_VALUE : useTickTimeout;
        return alreadyUsedTick > useTickLimit;
    }

    private void shouldStartUseItemNextTick() {
        this.useItemRemainingTicks = 0;
    }

    private void resetAlreadyUsedTick() {
        this.alreadyUsedTick = 0;
    }

    private void syncUseItemRemainingTicks(@NotNull ServerBot bot) {
        this.useItemRemainingTicks = bot.getUseItemRemainingTicks();
    }

    private void increaseAlreadyUsedTick() {
        this.alreadyUsedTick++;
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putInt("useTick", this.useTickTimeout);
        nbt.putInt("alreadyUsedTick", this.alreadyUsedTick);
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.useTickTimeout = nbt.getInt("useTick").orElseThrow();
        this.alreadyUsedTick = nbt.getInt("alreadyUsedTick").orElseGet(
            () -> this.useTickTimeout - nbt.getInt("tickToRelease").orElseThrow()
        );
    }

    public int getUseTickTimeout() {
        return useTickTimeout;
    }

    public void setUseTickTimeout(int useTickTimeout) {
        this.useTickTimeout = useTickTimeout;
    }

    @Override
    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        super.stop(bot, reason);
        bot.releaseUsingItem();
    }
}
