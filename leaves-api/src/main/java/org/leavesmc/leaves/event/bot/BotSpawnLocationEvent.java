package org.leavesmc.leaves.event.bot;

import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.entity.Bot;

public class BotSpawnLocationEvent extends BotEvent {

    private static final HandlerList handlers = new HandlerList();

    private Location spawnLocation;

    public BotSpawnLocationEvent(@NotNull final Bot who, @NotNull Location spawnLocation) {
        super(who);
        this.spawnLocation = spawnLocation;
    }

    @NotNull
    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(@NotNull Location location) {
        this.spawnLocation = location;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
