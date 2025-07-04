package org.leavesmc.leaves.entity.botaction;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

@org.jetbrains.annotations.ApiStatus.Experimental
public class LeavesBotAction {

    private final String actionName;
    private final UUID uuid;
    private final int initialTickDelay;
    private final int initialTickInterval;
    private final int initialNumber;
    private final @Nullable Consumer<LeavesBotAction> onSuccess;
    private final @Nullable Consumer<LeavesBotAction> onFail;

    private Player actionPlayer;
    private int tickToNext;
    private int numberRemaining;
    private boolean cancel;

    public LeavesBotAction(@NotNull BotActionType type, int initialTickInterval, int initialNumber) {
        this(type.getName(), UUID.randomUUID(), 0, initialTickInterval, initialNumber, null, null);
    }

    public LeavesBotAction(@NotNull BotActionType type, int initialTickInterval, int initialNumber, @Nullable Consumer<LeavesBotAction> onSuccess, @Nullable Consumer<LeavesBotAction> onFail) {
        this(type.getName(), UUID.randomUUID(), 0, initialTickInterval, initialNumber, onSuccess, onFail);
    }

    public LeavesBotAction(@NotNull BotActionType type, int initialTickDelay, int initialTickInterval, int initialNumber) {
        this(type.getName(), UUID.randomUUID(), initialTickDelay, initialTickInterval, initialNumber, null, null);
    }

    public LeavesBotAction(@NotNull BotActionType type, int initialTickDelay, int initialTickInterval, int initialNumber, @Nullable Consumer<LeavesBotAction> onSuccess, @Nullable Consumer<LeavesBotAction> onFail) {
        this(type.getName(), UUID.randomUUID(), initialTickDelay, initialTickInterval, initialNumber, onSuccess, onFail);
    }

    protected LeavesBotAction(String name, UUID actionUUID, int initialTickDelay, int initialTickInterval, int initialNumber, @Nullable Consumer<LeavesBotAction> onSuccess, @Nullable Consumer<LeavesBotAction> onFail) {
        this.actionName = name;
        this.uuid = actionUUID;
        this.initialTickDelay = initialTickDelay;
        this.initialTickInterval = initialTickInterval;
        this.initialNumber = initialNumber;
        this.onSuccess = onSuccess;
        this.onFail = onFail;
    }

    public String getActionName() {
        return actionName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getInitialTickDelay() {
        return initialTickDelay;
    }

    public int getInitialTickInterval() {
        return initialTickInterval;
    }

    public int getInitialNumber() {
        return initialNumber;
    }

    @Nullable
    public Player getActionPlayer() {
        return actionPlayer;
    }

    public void setActionPlayer(@Nullable Player actionPlayer) {
        this.actionPlayer = actionPlayer;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public int getNumberRemaining() {
        return numberRemaining;
    }

    public void setNumberRemaining(int numberRemaining) {
        this.numberRemaining = numberRemaining;
    }

    public int getTickToNext() {
        return tickToNext;
    }

    public void setTickToNext(int tickToNext) {
        this.tickToNext = tickToNext;
    }

    public @Nullable Consumer<LeavesBotAction> getOnSuccess() {
        return onSuccess;
    }

    public @Nullable Consumer<LeavesBotAction> getOnFail() {
        return onFail;
    }
}
