package org.leavesmc.leaves.event.bot;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.Bot;

import java.util.UUID;

public class BotActionStopEvent extends BotActionEvent  implements Cancellable{
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private final CommandSender sender;

    public enum Reason {
        DONE, COMMAND, PLUGIN, INTERNAL
    }

    private final Reason reason;

    public BotActionStopEvent(@NotNull Bot who, String actionName, UUID actionUUID, Reason stopReason, CommandSender sender) {
        super(who, actionName, actionUUID);
        this.reason = stopReason;
        this.sender = sender;
    }

    public Reason getReason() {
        return reason;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Nullable
    public CommandSender getSender() {
        return sender;
    }
}
