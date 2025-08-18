package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.event.bot.BotActionExecuteEvent;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;
import org.leavesmc.leaves.util.UpdateSuppressionException;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public abstract class ServerBotAction<E extends ServerBotAction<E>> {

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

    public ServerBotAction(String name, CommandArgument argument, Supplier<E> creator) {
        this.name = name;
        this.argument = argument;
        this.uuid = UUID.randomUUID();
        this.creator = creator;

        this.cancel = false;
        this.setStartDelayTick(0);
        this.setDoIntervalTick(1);
        this.setDoNumber(1);
    }

    public abstract boolean doTick(@NotNull ServerBot bot);

    public abstract Object asCraft();

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
                this.tickToNext = this.getDoIntervalTick();
                return;
            } else if (event.getResult() == BotActionExecuteEvent.Result.HARD_CANCEL) {
                if (this.numberRemaining > 0) {
                    this.numberRemaining--;
                }
                this.tickToNext = this.getDoIntervalTick();
                return;
            }

            boolean result = false;
            try {
                result = this.doTick(bot);
            } catch (UpdateSuppressionException e) {
                e.providePlayer(bot);
                e.consume();
            } catch (Exception e) {
                LeavesLogger.LOGGER.severe("An error occurred while executing bot " + bot.displayName + ", action " + this.name, e);
            }

            if (result) {
                if (this.numberRemaining > 0) {
                    this.numberRemaining--;
                }
                this.tickToNext = this.getDoIntervalTick();
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
        if (this.onStop != null) {
            this.onStop.accept((E) this);
        }
    }

    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) {
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

    public String getName() {
        return this.name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setStartDelayTick(int initialTickDelay) {
        this.initialTickDelay = initialTickDelay;
    }

    public int getStartDelayTick() {
        return this.initialTickDelay;
    }

    public void setDoIntervalTick(int initialTickInterval) {
        this.initialTickInterval = Math.max(0, initialTickInterval);
    }

    public int getDoIntervalTick() {
        return this.initialTickInterval;
    }

    public void setDoNumber(int initialNumber) {
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

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public void setOnFail(Consumer<E> onFail) {
        this.onFail = onFail;
    }

    public Consumer<E> getOnFail() {
        return onFail;
    }

    public void setOnSuccess(Consumer<E> onSuccess) {
        this.onSuccess = onSuccess;
    }

    public Consumer<E> getOnSuccess() {
        return onSuccess;
    }

    public void setOnStop(Consumer<E> onStop) {
        this.onStop = onStop;
    }

    public Consumer<E> getOnStop() {
        return onStop;
    }
}
