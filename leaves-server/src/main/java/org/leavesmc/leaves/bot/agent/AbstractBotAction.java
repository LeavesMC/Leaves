package org.leavesmc.leaves.bot.agent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.event.bot.BotActionExecuteEvent;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class AbstractBotAction<E extends AbstractBotAction<E>> {

    private final String name;
    private final CommandArgument argument;
    private final Supplier<E> creator;
    private UUID uuid;

    private int initialTickDelay;
    private int initialTickInterval;
    private int initialNumber;

    private int tickToNext;
    private int canDoNumber;
    private boolean cancel;

    public AbstractBotAction(String name, CommandArgument argument, Supplier<E> creator) {
        this.name = name;
        this.argument = argument;
        this.uuid = UUID.randomUUID();
        this.creator = creator;

        this.cancel = false;
        this.initialTickInterval = 20;
        this.initialNumber = -1;
    }

    public void init() {
        this.tickToNext = initialTickDelay;
        this.canDoNumber = this.getInitialNumber();
        this.setCancelled(false);
    }

    public void tryTick(ServerBot bot) {
        if (this.canDoNumber == 0) {
            this.stop(bot, BotActionStopEvent.Reason.DONE);
            return;
        }

        if (this.tickToNext <= 0) {
            BotActionExecuteEvent event = new BotActionExecuteEvent(bot.getBukkitEntity(), name, uuid);

            event.callEvent();
            if (event.getResult() == BotActionExecuteEvent.Result.SOFT_CANCEL) {
                this.tickToNext = this.getInitialTickInterval() - 1;
                return;
            } else if (event.getResult() == BotActionExecuteEvent.Result.HARD_CANCEL) {
                if (this.canDoNumber > 0) {
                    this.canDoNumber--;
                }
                this.tickToNext = this.getInitialTickInterval() - 1;
                return;
            }

            if (this.doTick(bot)) {
                if (this.canDoNumber > 0) {
                    this.canDoNumber--;
                }
                this.tickToNext = this.getInitialTickInterval() - 1;
            }
        } else {
            this.tickToNext--;
        }
    }

    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        if (!this.cancel) {
            nbt.putString("actionName", this.name);
            nbt.putUUID("actionUUID", this.uuid);

            nbt.putInt("initialTickDelay", this.initialTickDelay);
            nbt.putInt("initialTickInterval", this.initialTickInterval);
            nbt.putInt("initialNumber", this.initialNumber);

            nbt.putInt("tickToNext", this.tickToNext);
            nbt.putInt("canDoNumber", this.canDoNumber);
        }
        return nbt;
    }

    public void load(@NotNull CompoundTag nbt) {
        this.uuid = nbt.getUUID("actionUUID");

        this.initialTickDelay = nbt.getInt("initialTickDelay");
        this.initialTickInterval = nbt.getInt("initialTickInterval");
        this.initialNumber = nbt.getInt("initialNumber");

        this.tickToNext = nbt.getInt("tickToNext");
        this.canDoNumber = nbt.getInt("canDoNumber");
    }

    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        new BotActionStopEvent(bot.getBukkitEntity(), this.name, this.uuid, reason, null).callEvent();
        this.setCancelled(true);
    }

    public abstract void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result);

    public abstract boolean doTick(@NotNull ServerBot bot);

    @SuppressWarnings("unchecked")
    public E setTabComplete(int index, List<String> list) {
        this.argument.setTabComplete(index, list);
        return (E) this;
    }

    public String getName() {
        return this.name;
    }

    public UUID getUUID() {
        return uuid;
    }

    @SuppressWarnings("unchecked")
    public E setInitialTickDelay(int initialTickDelay) {
        this.initialTickDelay = initialTickDelay;
        return (E) this;
    }

    public int getInitialTickDelay() {
        return this.initialTickDelay;
    }

    public int getInitialTickInterval() {
        return this.initialTickInterval;
    }

    @SuppressWarnings("unchecked")
    public E setInitialTickInterval(int initialTickInterval) {
        this.initialTickInterval = Math.max(1, initialTickInterval);
        return (E) this;
    }

    public int getInitialNumber() {
        return this.initialNumber;
    }

    @SuppressWarnings("unchecked")
    public E setInitialNumber(int initialNumber) {
        this.initialNumber = Math.max(-1, initialNumber);
        return (E) this;
    }

    public int getTickToNext() {
        return this.tickToNext;
    }

    public int getCanDoNumber() {
        return this.canDoNumber;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public CommandArgument getArgument() {
        return this.argument;
    }

    @NotNull
    public E create() {
        return this.creator.get();
    }
}
