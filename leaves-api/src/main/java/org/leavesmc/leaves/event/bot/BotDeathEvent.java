package org.leavesmc.leaves.event.bot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.Bot;

public class BotDeathEvent extends BotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancel = false;
    private boolean sendDeathMessage;
    private Component deathMessage;

    public BotDeathEvent(@NotNull Bot who, @Nullable Component deathMessage, boolean sendDeathMessage) {
        super(who);
        this.deathMessage = deathMessage;
        this.sendDeathMessage = sendDeathMessage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Component deathMessage() {
        return deathMessage;
    }

    public void deathMessage(Component deathMessage) {
        this.deathMessage = deathMessage;
    }

    @Nullable
    public String getDeathMessage() {
        return this.deathMessage == null ? null : LegacyComponentSerializer.legacySection().serialize(this.deathMessage);
    }

    public void setDeathMessage(@Nullable String deathMessage) {
        this.deathMessage = deathMessage != null ? LegacyComponentSerializer.legacySection().deserialize(deathMessage) : null;
    }

    public boolean isSendDeathMessage() {
        return sendDeathMessage;
    }

    public void setSendDeathMessage(boolean sendDeathMessage) {
        this.sendDeathMessage = sendDeathMessage;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
