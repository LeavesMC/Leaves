package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface DropAction extends TimerBotAction<DropAction> {
    static DropAction create() {
        return Bukkit.getBotManager().newAction(DropAction.class);
    }
}
