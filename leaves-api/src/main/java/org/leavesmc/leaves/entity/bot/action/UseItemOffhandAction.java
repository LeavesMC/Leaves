package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot only uses the item in its offhand, without using it on blocks or entities.
 */
public interface UseItemOffhandAction extends TimerBotAction<UseItemOffhandAction> {
    static UseItemOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemOffhandAction.class);
    }
}
