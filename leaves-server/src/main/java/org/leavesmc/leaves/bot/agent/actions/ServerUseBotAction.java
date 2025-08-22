package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;
import org.leavesmc.leaves.neo_command.CommandContext;

import java.util.function.Supplier;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

public abstract class ServerUseBotAction<T extends ServerUseBotAction<T>> extends ServerTimerBotAction<T> {
    private int useTickTimeout = -1;
    private int alreadyUsedTick = 0;
    private int useItemRemainingTicks = 0;

    public ServerUseBotAction(String name, Supplier<T> supplier) {
        super(name, supplier);
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
