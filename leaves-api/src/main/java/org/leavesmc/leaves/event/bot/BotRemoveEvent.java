package org.leavesmc.leaves.event.bot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.bot.Bot;

/**
 * Call when a fakeplayer removed
 */
public class BotRemoveEvent extends BotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final RemoveReason reason;
    private final CommandSender remover;
    private Component removeMessage;
    private boolean save;
    private boolean cancel = false;

    public BotRemoveEvent(@NotNull final Bot who, @NotNull RemoveReason reason, @Nullable CommandSender remover, @Nullable Component removeMessage, boolean save) {
        super(who);
        this.reason = reason;
        this.remover = remover;
        this.removeMessage = removeMessage;
        this.save = save;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets the remove reason of the bot
     *
     * @return remove reason
     */
    @NotNull
    public RemoveReason getReason() {
        return reason;
    }

    /**
     * Gets the remover of the bot
     * if the remove reason is not COMMAND, the creator might be null
     *
     * @return An optional of remover
     */
    @Nullable
    public CommandSender getRemover() {
        return remover;
    }

    public Component removeMessage() {
        return removeMessage;
    }

    public void removeMessage(Component removeMessage) {
        this.removeMessage = removeMessage;
    }

    @Nullable
    public String getRemoveMessage() {
        return this.removeMessage == null ? null : LegacyComponentSerializer.legacySection().serialize(this.removeMessage);
    }

    public void setRemoveMessage(@Nullable String removeMessage) {
        this.removeMessage = removeMessage != null ? LegacyComponentSerializer.legacySection().deserialize(removeMessage) : null;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean shouldSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public enum RemoveReason {
        COMMAND,
        PLUGIN,
        DEATH,
        INTERNAL
    }
}
