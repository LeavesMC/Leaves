package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action for a bot to float up in water.
 */
public interface SwimAction extends StateBotAction<SwimAction> {
    static SwimAction create() {
        return Bukkit.getBotManager().newAction(SwimAction.class);
    }
}
