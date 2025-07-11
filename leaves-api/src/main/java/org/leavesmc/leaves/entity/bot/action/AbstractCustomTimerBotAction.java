package org.leavesmc.leaves.entity.bot.action;

import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractCustomTimerBotAction<T extends AbstractCustomTimerBotAction<T>> implements TimerBotAction<T>, CustomBotAction {
    private boolean cancelled = false;
    private int startDelayTick = 0;
    private int doIntervalTick = 20;
    private int doNumber = -1;
    private Consumer<T> onFail = null;
    private Consumer<T> onSuccess = null;
    private Consumer<T> onStop = null;

    @Override
    public final UUID getUUID() {
        throw new UnsupportedOperationException("getUUID() is not supported in CustomTimerBotAction");
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setOnFail(Consumer<T> onFail) {
        this.onFail = onFail;
    }

    @Override
    public Consumer<T> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<T> onSuccess) {
        this.onSuccess = onSuccess;
    }

    @Override
    public Consumer<T> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<T> onStop) {
        this.onStop = onStop;
    }

    @Override
    public Consumer<T> getOnStop() {
        return onStop;
    }

    @Override
    public void setStartDelayTick(int delayTick) {
        this.startDelayTick = delayTick;
    }

    @Override
    public int getStartDelayTick() {
        return startDelayTick;
    }

    @Override
    public void setDoIntervalTick(int intervalTick) {
        this.doIntervalTick = intervalTick;
    }

    @Override
    public int getDoIntervalTick() {
        return doIntervalTick;
    }

    @Override
    public void setDoNumber(int doNumber) {
        this.doNumber = doNumber;
    }

    @Override
    public int getDoNumber() {
        return doNumber;
    }

    @Override
    public int getTickToNext() {
        return startDelayTick;
    }

    @Override
    public int getDoNumberRemaining() {
        return doNumber;
    }
}
