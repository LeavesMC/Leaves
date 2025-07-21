package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action for a bot to perform auto fishing.
 */
public interface FishAction extends TimerBotAction<FishAction> {
    static FishAction create() {
        return Bukkit.getBotManager().newAction(FishAction.class);
    }
}
