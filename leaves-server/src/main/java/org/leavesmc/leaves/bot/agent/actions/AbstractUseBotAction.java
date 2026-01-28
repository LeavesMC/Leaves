package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.ExtraData;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

public abstract class AbstractUseBotAction<T extends AbstractUseBotAction<T>> extends AbstractTimerBotAction<T> {
    private int useTickTimeout = -1;
    private int alreadyUsedTick = 0;
    private int useItemRemainingTicks = 0;

    public AbstractUseBotAction(String name) {
        super(name);
        this.addArgument("use_timeout", integer(-1))
            .suggests((context, builder) -> {
                builder.suggest("-1", Component.literal("no use timeout"));
                builder.suggest("3", Component.literal("minimum bow shoot time"));
                builder.suggest("10", Component.literal("minimum trident shoot time"));
            })
            .setOptional(true);
    }

    @Override
    public void loadCommand(@NotNull CommandContext context) {
        super.loadCommand(context);
        this.useTickTimeout = context.getIntegerOrDefault("use_timeout", -1);
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
    public String getActionDataString(@NotNull ExtraData data) {
        data.add("use_timeout", String.valueOf(this.useTickTimeout));
        data.add("already_used_tick", String.valueOf(this.alreadyUsedTick));
        return super.getActionDataString(data);
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
        this.useTickTimeout = nbt.getIntOr("useTick", this.useTickTimeout);
        this.alreadyUsedTick = nbt.getInt("alreadyUsedTick").orElseGet(
            () -> nbt.getInt("tickToRelease").map(tickToRelease -> this.useTickTimeout - tickToRelease).orElse(this.alreadyUsedTick)
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
