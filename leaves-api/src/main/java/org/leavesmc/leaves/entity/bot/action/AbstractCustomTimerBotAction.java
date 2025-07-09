package org.leavesmc.leaves.entity.bot.action;

import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractCustomTimerBotAction implements TimerBotAction<AbstractCustomTimerBotAction>, CustomBotAction<AbstractCustomTimerBotAction> {
    private boolean cancelled = false;
    private int startDelayTick = 0, doIntervalTick = 0, doNumber = 0;
    private Consumer<AbstractCustomTimerBotAction> onFail = null, onSuccess = null, onStop = null;

    @Override
    public final UUID getUUID() {
        throw  new UnsupportedOperationException("getUUID() is not supported in CustomTimerBotAction");
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
    public void setOnFail(Consumer<AbstractCustomTimerBotAction> onFail) {
        this.onFail = onFail;
    }

    @Override
    public Consumer<AbstractCustomTimerBotAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<AbstractCustomTimerBotAction> onSuccess) {
        this.onSuccess = onSuccess;
    }

    @Override
    public Consumer<AbstractCustomTimerBotAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<AbstractCustomTimerBotAction> onStop) {
        this.onStop = onStop;
    }

    @Override
    public Consumer<AbstractCustomTimerBotAction> getOnStop() {
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
