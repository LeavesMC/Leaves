package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface UseItemOnOffhandAction extends TimerBotAction<UseItemOnOffhandAction> {
    static UseItemOnOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemOnOffhandAction.class);
    }
}
