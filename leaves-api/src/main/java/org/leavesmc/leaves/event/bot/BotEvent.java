package org.leavesmc.leaves.event.bot;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.entity.Bot;

/**
 * Represents a fakeplayer related event
 */
public abstract class BotEvent extends Event {

    protected Bot bot;

    public BotEvent(@NotNull final Bot who) {
        bot = who;
    }

    public BotEvent(@NotNull final Bot who, boolean async) {
        super(async);
        bot = who;
    }

    /**
     * Returns the fakeplayer involved in this event
     *
     * @return Fakeplayer who is involved in this event
     */
    @NotNull
    public final Bot getBot() {
        return bot;
    }
}
