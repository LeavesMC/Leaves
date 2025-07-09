package org.leavesmc.leaves.entity.bot.action;

import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractCustomBotAction implements BotAction<AbstractCustomBotAction>, CustomBotAction<AbstractCustomBotAction> {
    private boolean cancelled = false;
    private Consumer<AbstractCustomBotAction> onFail = null, onSuccess = null, onStop = null;

    @Override
    public final UUID getUUID() {
        throw  new UnsupportedOperationException("getUUID() is not supported in CustomBotAction");
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
    public void setOnFail(Consumer<AbstractCustomBotAction> onFail) {
        this.onFail = onFail;
    }

    @Override
    public Consumer<AbstractCustomBotAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<AbstractCustomBotAction> onSuccess) {
        this.onSuccess = onSuccess;
    }

    @Override
    public Consumer<AbstractCustomBotAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<AbstractCustomBotAction> onStop) {
        this.onStop = onStop;
    }

    @Override
    public Consumer<AbstractCustomBotAction> getOnStop() {
        return onStop;
    }
}
