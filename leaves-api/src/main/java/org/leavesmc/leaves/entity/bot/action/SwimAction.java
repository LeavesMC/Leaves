package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface SwimAction extends StateBotAction<SwimAction> {
    static SwimAction create() {
        return Bukkit.getBotManager().newAction(SwimAction.class);
    }
}
