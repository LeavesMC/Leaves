package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface UseItemToOffhandAction extends TimerBotAction<UseItemToOffhandAction> {
    static UseItemToOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemToOffhandAction.class);
    }
}
