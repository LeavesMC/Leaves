package org.leavesmc.leaves.event.bot;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.Bot;

import java.util.UUID;

public class BotActionScheduleEvent extends BotActionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancel = false;
    private final CommandSender sender;

    public BotActionScheduleEvent(@NotNull Bot who, String actionName, UUID actionUUID, CommandSender sender) {
        super(who, actionName, actionUUID);
        this.sender = sender;
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

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
