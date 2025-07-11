package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface SneakAction extends StateBotAction<SneakAction> {
    static SneakAction create() {
        return Bukkit.getBotManager().newAction(SneakAction.class);
    }
}
