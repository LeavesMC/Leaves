package org.leavesmc.leaves.event.bot;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Call when a fakeplayer loaded
 */
public class BotLoadEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final String bot;
    private final UUID botUUID;
    private boolean cancel = false;

    public BotLoadEvent(@NotNull final String who, @NotNull final UUID uuid) {
        this.bot = who;
        this.botUUID = uuid;
    }

    /**
     * Gets the fakeplayer name
     *
     * @return fakeplayer name
     */
    public String getBot() {
        return bot;
    }

    public UUID getBotUUID() {
        return botUUID;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
