package org.leavesmc.leaves.event.bot;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Call when a fakeplayer created
 */
public class BotCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public enum CreateReason {
        COMMAND,
        PLUGIN,
        INTERNAL,
        UNKNOWN,
    }

    private final String bot;
    private final String skin;
    private final CreateReason reason;
    private final CommandSender creator;
    private Location createLocation;
    private boolean cancel = false;

    public BotCreateEvent(@NotNull final String who, @NotNull final String skin, @NotNull final Location createLocation, @NotNull CreateReason reason, @Nullable CommandSender creator) {
        this.bot = who;
        this.skin = skin;
        this.createLocation = createLocation;
        this.reason = reason;
        this.creator = creator;
    }

    /**
     * Gets the fakeplayer name
     *
     * @return fakeplayer name
     */
    public String getBot() {
        return bot;
    }

    /**
     * Gets the location to create the fakeplayer
     *
     * @return Location to create the fakeplayer
     */
    @NotNull
    public Location getCreateLocation() {
        return createLocation;
    }

    /**
     * Sets the location to create the fakeplayer
     *
     * @param createLocation location to create the fakeplayer
     */
    public void setCreateLocation(@NotNull Location createLocation) {
        this.createLocation = createLocation;
    }

    /**
     * Gets the fakeplayer skin
     *
     * @return fakeplayer skin name
     */
    @Nullable
    public String getSkin() {
        return skin;
    }

    /**
     * Gets the create reason of the bot
     *
     * @return create reason
     */
    @NotNull
    public CreateReason getReason() {
        return reason;
    }

    /**
     * Gets the creator of the bot
     * if the create reason is not COMMAND, the creator might be null
     *
     * @return An optional of creator
     */
    @Nullable
    public CommandSender getCreator() {
        return creator;
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
