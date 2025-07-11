package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface UseItemToAction extends TimerBotAction<UseItemToAction> {
    static UseItemToAction create() {
        return Bukkit.getBotManager().newAction(UseItemToAction.class);
    }
}
