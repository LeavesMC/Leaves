package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

public interface ShootAction extends TimerBotAction<ShootAction> {

    /**
     * Gets the drawing tick for the shoot action.
     *
     * @return the drawing tick
     */
    int getDrawingTick();

    /**
     * Sets the drawing tick for the shoot action.
     *
     * @param drawingTick the drawing tick to set
     */
    ShootAction setDrawingTick(int drawingTick);

    static ShootAction create() {
        return Bukkit.getBotManager().newAction(ShootAction.class);
    }
}
