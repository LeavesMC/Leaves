package org.leavesmc.leaves.event.bot;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.bot.Bot;

import java.util.UUID;

public class BotActionStopEvent extends BotActionEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final CommandSender sender;
    private final Reason reason;
    private boolean cancel = false;

    public BotActionStopEvent(@NotNull Bot who, String actionName, UUID actionUUID, Reason stopReason, CommandSender sender) {
        super(who, actionName, actionUUID);
        this.reason = stopReason;
        this.sender = sender;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Reason getReason() {
        return reason;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
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

    public enum Reason {
        DONE, COMMAND, PLUGIN, INTERNAL
    }
}
