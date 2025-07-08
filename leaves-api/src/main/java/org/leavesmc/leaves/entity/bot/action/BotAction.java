package org.leavesmc.leaves.entity.bot.action;

import java.util.UUID;
import java.util.function.Consumer;

public interface BotAction<T> {

    String getName();

    UUID getUUID();

    void setCancelled(boolean cancel);

    boolean isCancelled();

    void setOnFail(Consumer<T> onFail);

    Consumer<T> getOnFail();

    void setOnSuccess(Consumer<T> onSuccess);

    Consumer<T> getOnSuccess();

    void setOnStop(Consumer<T> onStop);

    Consumer<T> getOnStop();
}
