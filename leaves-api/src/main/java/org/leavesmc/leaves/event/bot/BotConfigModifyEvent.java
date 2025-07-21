package org.leavesmc.leaves.event.bot;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.entity.bot.Bot;

public class BotConfigModifyEvent extends BotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final String configName;
    private final String[] configValue;
    private final CommandSender sender;
    private boolean cancel;

    public BotConfigModifyEvent(@NotNull Bot who, String configName, String[] configValue, CommandSender sender) {
        super(who);
        this.configName = configName;
        this.configValue = configValue;
        this.sender = sender;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public String getConfigName() {
        return configName;
    }

    @NotNull
    public String[] getConfigValue() {
        return configValue;
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
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
