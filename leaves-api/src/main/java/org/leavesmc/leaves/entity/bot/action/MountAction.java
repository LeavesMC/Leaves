package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action for a bot to mount to nearby vehicles.
 */
public interface MountAction extends BotAction<MountAction> {
    static MountAction create() {
        return Bukkit.getBotManager().newAction(MountAction.class);
    }
}
