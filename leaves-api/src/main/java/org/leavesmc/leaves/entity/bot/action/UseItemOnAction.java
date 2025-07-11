package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface UseItemOnAction extends TimerBotAction<UseItemOnAction> {
    static UseItemOnAction create() {
        return Bukkit.getBotManager().newAction(UseItemOnAction.class);
    }
}
