package org.leavesmc.leaves.entity.botaction;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LeavesBotAction {

    private final String actionName;
    private int tickToExecute;
    private int executeInterval;
    private int remainingExecuteTime;
    private final UUID uuid;
    private Player actionPlayer;

    public LeavesBotAction(BotActionType type, int executeInterval, int remainingExecuteTime) {
        this(type.getName(), executeInterval, remainingExecuteTime, UUID.randomUUID());
    }

    public LeavesBotAction(String name, int executeInterval, int remainingExecuteTime) {
        this(name, executeInterval, remainingExecuteTime, UUID.randomUUID());
    }

    protected LeavesBotAction(String name, int executeInterval, int remainingExecuteTime, UUID actionUUID) {
        this.actionName = name;
        this.remainingExecuteTime = remainingExecuteTime;
        this.executeInterval = executeInterval;
        this.uuid = actionUUID;
        this.tickToExecute = executeInterval;
    }

    public void setTickToExecute(int tickToExecute) {
        this.tickToExecute = tickToExecute;
    }

    public int getTickToExecute() {
        return tickToExecute;
    }

    public void setExecuteInterval(int executeInterval) {
        this.executeInterval = executeInterval;
    }

    public int getExecuteInterval() {
        return executeInterval;
    }

    public void setRemainingExecuteTime(int remainingExecuteTime) {
        this.remainingExecuteTime = remainingExecuteTime;
    }

    public int getRemainingExecuteTime() {
        return remainingExecuteTime;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionPlayer(@Nullable Player actionPlayer) {
        this.actionPlayer = actionPlayer;
    }

    @Nullable
    public Player getActionPlayer() {
        return actionPlayer;
    }

    public UUID getUuid() {
        return uuid;
    }
}
