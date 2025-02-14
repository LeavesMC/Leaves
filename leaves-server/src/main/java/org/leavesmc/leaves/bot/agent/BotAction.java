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

public abstract class BotAction<E extends BotAction<E>> {

    private final String name;
    private final CommandArgument argument;
    private final Supplier<E> creator;

    private boolean cancel;
    private int tickDelay;
    private int number;
    private UUID uuid;

    private int needWaitTick;
    private int canDoNumber;

    public BotAction(String name, CommandArgument argument, Supplier<E> creator) {
        this.name = name;
        this.argument = argument;
        this.uuid = UUID.randomUUID();
        this.creator = creator;

        this.cancel = false;
        this.tickDelay = 20;
        this.number = -1;
    }

    public void init() {
        this.needWaitTick = 0;
        this.canDoNumber = this.getNumber();
        this.setCancelled(false);
    }

    public String getName() {
        return this.name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getTickDelay() {
        return this.tickDelay;
    }

    @SuppressWarnings("unchecked")
    public E setTickDelay(int tickDelay) {
        this.tickDelay = Math.max(0, tickDelay);
        return (E) this;
    }

    public int getNumber() {
        return this.number;
    }

    @SuppressWarnings("unchecked")
    public E setNumber(int number) {
        this.number = Math.max(-1, number);
        return (E) this;
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

    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        new BotActionStopEvent(bot.getBukkitEntity(), this.name, this.uuid, reason, null).callEvent();
        this.setCancelled(true);
    }

    public CommandArgument getArgument() {
        return this.argument;
    }

    @SuppressWarnings("unchecked")
    public E setTabComplete(int index, List<String> list) {
        this.argument.setTabComplete(index, list);
        return (E) this;
    }

    public void tryTick(ServerBot bot) {
        if (this.canDoNumber == 0) {
            this.stop(bot, BotActionStopEvent.Reason.DONE);
            return;
        }

        if (this.needWaitTick <= 0) {
            BotActionExecuteEvent event = new BotActionExecuteEvent(bot.getBukkitEntity(), name, uuid);

            event.callEvent();
            if (event.getResult() == BotActionExecuteEvent.Result.SOFT_CANCEL) {
                this.needWaitTick = this.getTickDelay();
                return;
            } else if (event.getResult() == BotActionExecuteEvent.Result.HARD_CANCEL) {
                if (this.canDoNumber > 0) {
                    this.canDoNumber--;
                }
                this.needWaitTick = this.getTickDelay();
                return;
            }

            if (this.doTick(bot)) {
                if (this.canDoNumber > 0) {
                    this.canDoNumber--;
                }
                this.needWaitTick = this.getTickDelay();
            }
        } else {
            this.needWaitTick--;
        }
    }

    @NotNull
    public E create() {
        return this.creator.get();
    }

    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        if (!this.cancel) {
            nbt.putString("actionName", this.name);
            nbt.putUUID("actionUUID", this.uuid);

            nbt.putInt("canDoNumber", this.canDoNumber);
            nbt.putInt("needWaitTick", this.needWaitTick);
            nbt.putInt("tickDelay", this.tickDelay);
        }
        return nbt;
    }

    public void load(@NotNull CompoundTag nbt) {
        this.tickDelay = nbt.getInt("tickDelay");
        this.needWaitTick = nbt.getInt("needWaitTick");
        this.canDoNumber = nbt.getInt("canDoNumber");
        this.uuid = nbt.getUUID("actionUUID");
    }

    public abstract void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result);

    public abstract boolean doTick(@NotNull ServerBot bot);
}
