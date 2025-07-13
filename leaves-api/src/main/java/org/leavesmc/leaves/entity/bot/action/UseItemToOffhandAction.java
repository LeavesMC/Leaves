package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item in its offhand to an entity.
 */
public interface UseItemToOffhandAction extends TimerBotAction<UseItemToOffhandAction> {
    static UseItemToOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemToOffhandAction.class);
    }
}
