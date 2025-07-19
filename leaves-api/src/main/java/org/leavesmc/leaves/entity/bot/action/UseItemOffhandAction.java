package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action where a bot only uses the item in its offhand, without using it on blocks or entities.
 */
public interface UseItemOffhandAction extends TimerBotAction<UseItemOffhandAction> {

    /**
     * Gets the equivalent right-click hold duration in ticks.
     * Default is -1, which means will not be released.
     *
     * @return the equivalent right-click hold duration
     */
    int getUseTick();

    /**
     * Sets the equivalent right-click hold duration in ticks.
     *
     * @param useTick the equivalent right-click hold duration
     * @return this action instance
     */
    UseItemOffhandAction setUseTick(int useTick);

    static UseItemOffhandAction create() {
        return Bukkit.getBotManager().newAction(UseItemOffhandAction.class);
    }
}
