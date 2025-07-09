package org.leavesmc.leaves.entity.bot.action;

import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractCustomStateBotAction implements StateBotAction<AbstractCustomStateBotAction>, CustomBotAction<AbstractCustomStateBotAction> {
    private boolean cancelled = false;
    private Consumer<AbstractCustomStateBotAction> onFail = null, onSuccess = null, onStop = null;

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
    public void setOnFail(Consumer<AbstractCustomStateBotAction> onFail) {
        this.onFail = onFail;
    }

    @Override
    public Consumer<AbstractCustomStateBotAction> getOnFail() {
        return onFail;
    }

    @Override
    public void setOnSuccess(Consumer<AbstractCustomStateBotAction> onSuccess) {
        this.onSuccess = onSuccess;
    }

    @Override
    public Consumer<AbstractCustomStateBotAction> getOnSuccess() {
        return onSuccess;
    }

    @Override
    public void setOnStop(Consumer<AbstractCustomStateBotAction> onStop) {
        this.onStop = onStop;
    }

    @Override
    public Consumer<AbstractCustomStateBotAction> getOnStop() {
        return onStop;
    }
}
