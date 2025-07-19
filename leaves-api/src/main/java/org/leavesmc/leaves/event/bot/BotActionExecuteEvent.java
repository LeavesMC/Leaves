package org.leavesmc.leaves.event.bot;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.entity.bot.Bot;

import java.util.UUID;

public class BotActionExecuteEvent extends BotActionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Result result = Result.PASS;

    public BotActionExecuteEvent(@NotNull Bot who, String actionName, UUID actionUUID) {
        super(who, actionName, actionUUID);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return result != Result.PASS;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.result = cancel ? Result.SOFT_CANCEL : Result.PASS;
    }

    public void hardCancel() {
        this.result = Result.HARD_CANCEL;
    }

    public Result getResult() {
        return this.result;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public enum Result {
        PASS, SOFT_CANCEL, HARD_CANCEL

    }
}

