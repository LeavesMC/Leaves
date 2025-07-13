package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action for a bot to shoot a projectile, such as use bow to shoot an arrow.
 */
public interface ShootAction extends TimerBotAction<ShootAction> {

    /**
     * Gets the drawing tick for the shoot action.
     * <p>
     * The drawing tick determines how long the bot will draw the bow before shooting.
     *
     * @return the drawing tick
     */
    int getDrawingTick();

    /**
     * Sets the drawing tick for the shoot action.
     * <p>
     * The drawing tick determines how long the bot will draw the bow before shooting.
     *
     * @param drawingTick the drawing tick to set
     * @return this action instance
     */
    ShootAction setDrawingTick(int drawingTick);

    static ShootAction create() {
        return Bukkit.getBotManager().newAction(ShootAction.class);
    }
}
