package org.leavesmc.leaves.entity.bot.action;

import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractCustomStateBotAction<T extends AbstractCustomStateBotAction<T>> implements StateBotAction<T>, CustomBotAction {
    private boolean cancelled = false;
    private Consumer<T> onFail = null;
    private Consumer<T> onSuccess = null;
    private Consumer<T> onStop = null;

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
}
