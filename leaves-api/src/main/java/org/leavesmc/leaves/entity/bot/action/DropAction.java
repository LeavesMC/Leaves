package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action for a bot to drop all items in its inventory.
 */
public interface DropAction extends TimerBotAction<DropAction> {
    static DropAction create() {
        return Bukkit.getBotManager().newAction(DropAction.class);
    }
}
