package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot only uses an item, without using it on blocks or entities.
 */
public interface UseItemAction extends TimerBotAction<UseItemAction> {
    static UseItemAction create() {
        return Bukkit.getBotManager().newAction(UseItemAction.class);
    }
}
