package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.entity.bot.action.BotAction;
import org.leavesmc.leaves.event.bot.BotActionExecuteEvent;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public abstract class CraftBotAction<E extends BotAction<E>> implements BotAction<E> {

    private final String name;
    private final CommandArgument argument;
    private final Supplier<E> creator;
    private UUID uuid;

    private int initialTickDelay;
    private int initialTickInterval;
    private int initialNumber;

    private int tickToNext;
    private int numberRemaining;
    private boolean cancel;

    private Consumer<E> onFail;
    private Consumer<E> onSuccess;
    private Consumer<E> onStop;

    public CraftBotAction(String name, CommandArgument argument, Supplier<E> creator) {
        this.name = name;
        this.argument = argument;
        this.uuid = UUID.randomUUID();
        this.creator = creator;

        this.cancel = false;
        this.setStartDelayTick0(0);
        this.setDoIntervalTick0(1);
        this.setDoNumber0(1);
    }

    public void init() {
        this.tickToNext = initialTickDelay;
        this.numberRemaining = this.getDoNumber();
        this.setCancelled(false);
    }

    public void tryTick(ServerBot bot) {
        if (this.numberRemaining == 0) {
            this.stop(bot, BotActionStopEvent.Reason.DONE);
            return;
        }

        if (this.cancel) {
            this.stop(bot, BotActionStopEvent.Reason.PLUGIN);
            return;
        }

        if (this.tickToNext <= 0) {
            BotActionExecuteEvent event = new BotActionExecuteEvent(bot.getBukkitEntity(), name, uuid);

            event.callEvent();
            if (event.getResult() == BotActionExecuteEvent.Result.SOFT_CANCEL) {
                this.tickToNext = this.getDoIntervalTick() - 1;
                return;
            } else if (event.getResult() == BotActionExecuteEvent.Result.HARD_CANCEL) {
                if (this.numberRemaining > 0) {
                    this.numberRemaining--;
                }
                this.tickToNext = this.getDoIntervalTick() - 1;
                return;
            }

            if (this.doTick(bot)) {
                if (this.numberRemaining > 0) {
                    this.numberRemaining--;
                }
                this.tickToNext = this.getDoIntervalTick() - 1;
                if (this.onSuccess != null) {
                    this.onSuccess.accept((E) this);
                }
            } else if (this.onFail != null) {
                this.onFail.accept((E) this);
            }
        } else {
            this.tickToNext--;
        }
    }

    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        if (!this.cancel) {
            nbt.putString("actionName", this.name);
            nbt.store("actionUUID", UUIDUtil.CODEC, this.uuid);

            nbt.putInt("initialTickDelay", this.initialTickDelay);
            nbt.putInt("initialTickInterval", this.initialTickInterval);
            nbt.putInt("initialNumber", this.initialNumber);

            nbt.putInt("tickToNext", this.tickToNext);
            nbt.putInt("numberRemaining", this.numberRemaining);
        }
        return nbt;
    }

    public void load(@NotNull CompoundTag nbt) {
        this.uuid = nbt.read("actionUUID", UUIDUtil.CODEC).orElse(UUID.randomUUID());

        this.initialTickDelay = nbt.getInt("initialTickDelay").orElse(0);
        this.initialTickInterval = nbt.getInt("initialTickInterval").orElse(0);
        this.initialNumber = nbt.getInt("initialNumber").orElse(0);

        this.tickToNext = nbt.getInt("tickToNext").orElse(0);
        this.numberRemaining = nbt.getInt("numberRemaining").orElse(0);
    }

    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        new BotActionStopEvent(bot.getBukkitEntity(), this.name, this.uuid, reason, null).callEvent();
        this.setCancelled(true);
        if (this.onStop != null) this.onStop.accept((E) this);
        this.onStop(bot, reason);
    }

    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) {
    }

    public abstract boolean doTick(@NotNull ServerBot bot);

    public abstract Class<E> getInterfaceClass();

    public void onStop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
    }

    public void setSuggestion(int n, BiFunction<CommandSender, String, Pair<List<String>, String>> suggestion) {
        this.argument.setSuggestion(n, suggestion);
    }

    public void setSuggestion(int n, Pair<List<String>, String> suggestion) {
        this.setSuggestion(n, (sender, arg) -> suggestion);
    }

    public void setSuggestion(int n, List<String> tabComplete) {
        this.setSuggestion(n, Pair.of(tabComplete, null));
    }

    @NotNull
    public E create() {
        return this.creator.get();
    }

    public CommandArgument getArgument() {
        return this.argument;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    public void setStartDelayTick0(int initialTickDelay) {
        this.initialTickDelay = initialTickDelay;
    }

    public int getStartDelayTick() {
        return this.initialTickDelay;
    }

    public void setDoIntervalTick0(int initialTickInterval) {
        this.initialTickInterval = Math.max(1, initialTickInterval);
    }

    public int getDoIntervalTick() {
        return this.initialTickInterval;
    }

    public void setDoNumber0(int initialNumber) {
        this.initialNumber = Math.max(-1, initialNumber);
    }

    public int getDoNumber() {
        return this.initialNumber;
    }

    public int getTickToNext() {
        return this.tickToNext;
    }

    public int getDoNumberRemaining() {
        return this.numberRemaining;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public void setOnFail(Consumer<E> onFail) {
        this.onFail = onFail;
    }

    @Override
    public Consumer<E> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<E> onSuccess) {
        this.onSuccess = onSuccess;
    }

    @Override
    public Consumer<E> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<E> onStop) {
        this.onStop = onStop;
    }

    @Override
    public Consumer<E> getOnStop() {
        return onStop;
    }
}
