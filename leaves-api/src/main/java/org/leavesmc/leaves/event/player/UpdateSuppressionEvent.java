package org.leavesmc.leaves.event.player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UpdateSuppressionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final @Nullable Player player;
    private final @Nullable Location position;
    private final @Nullable Material material;
    private final @NotNull Throwable throwable;

    public UpdateSuppressionEvent(@Nullable Player player, @Nullable Location position, @Nullable Material material, @NotNull Throwable throwable) {
        this.player = player;
        this.position = position;
        this.material = material;
        this.throwable = throwable;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public @Nullable Player getPlayer() {
        return player;
    }

    public @NotNull Throwable getThrowable() {
        return throwable;
    }

    public @Nullable Location getPosition() {
        return position;
    }

    public @Nullable Material getMaterial() {
        return material;
    }
}
