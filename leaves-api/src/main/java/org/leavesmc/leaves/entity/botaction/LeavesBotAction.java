package org.leavesmc.leaves.entity.botaction;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@org.jetbrains.annotations.ApiStatus.Experimental
public class LeavesBotAction {

    private final String actionName;
    private final UUID uuid;
    private final int initialTickDelay;
    private final int initialTickInterval;
    private final int initialNumber;

    private Player actionPlayer;
    private int tickToNext;
    private int numberRemaining;
    private boolean cancel;

    public LeavesBotAction(BotActionType type, int initialTickInterval, int initialNumber) {
        this(type.getName(), UUID.randomUUID(), 0, initialTickInterval, initialNumber);
    }

    public LeavesBotAction(BotActionType type, int initialTickDelay, int initialTickInterval, int initialNumber) {
        this(type.getName(), UUID.randomUUID(), initialTickDelay, initialTickInterval, initialNumber);
    }

    protected LeavesBotAction(String name, UUID actionUUID, int initialTickDelay, int initialTickInterval, int initialNumber) {
        this.actionName = name;
        this.uuid = actionUUID;
        this.initialTickDelay = initialTickDelay;
        this.initialTickInterval = initialTickInterval;
        this.initialNumber = initialNumber;
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
}
