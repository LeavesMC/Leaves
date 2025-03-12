package org.leavesmc.leaves.event.bot;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.entity.Bot;

import java.util.UUID;

public abstract class BotActionEvent extends BotEvent {

    private final String actionName;
    private final UUID actionUUID;

    public BotActionEvent(@NotNull Bot who, String actionName, UUID actionUUID) {
        super(who);
        this.actionName = actionName;
        this.actionUUID = actionUUID;
    }

    @NotNull
    public String getActionName() {
        return actionName;
    }

    public UUID getActionUUID() {
        return actionUUID;
    }
}
