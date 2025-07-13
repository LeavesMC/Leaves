package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot uses an item, fully simulating the effect of a player right-clicking.
 */
public interface UseItemAutoAction extends TimerBotAction<UseItemAutoAction> {
    static UseItemAutoAction create() {
        return Bukkit.getBotManager().newAction(UseItemAutoAction.class);
    }
}
