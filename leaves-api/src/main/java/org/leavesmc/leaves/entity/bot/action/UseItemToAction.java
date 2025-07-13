package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item to an entity.
 */
public interface UseItemToAction extends TimerBotAction<UseItemToAction> {
    static UseItemToAction create() {
        return Bukkit.getBotManager().newAction(UseItemToAction.class);
    }
}
