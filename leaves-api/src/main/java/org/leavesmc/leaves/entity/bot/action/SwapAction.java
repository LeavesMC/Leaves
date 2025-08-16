package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action for a bot to swap items between the main hand and off-hand.
 */
public interface SwapAction extends BotAction<SwapAction> {
    static SwapAction create() {
        return Bukkit.getBotManager().newAction(SwapAction.class);
    }
}
