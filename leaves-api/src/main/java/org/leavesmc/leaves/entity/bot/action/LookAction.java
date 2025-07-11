package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public interface LookAction extends BotAction<LookAction> {

    /**
     * Sets the position to look at.
     *
     * @param pos the position to look at
     * @return this action instance for method chaining
     */
    LookAction setPos(Vector pos);

    /**
     * Gets the position to look at.
     *
     * @return the position to look at
     */
    Vector getPos();

    /**
     * Sets the player to look to.
     * When set to a player, the bot will look at the player's current position,
     * which will override the position set by {@link #setPos(Vector)}.
     *
     * @param player the player to set
     * @return this action instance for method chaining
     */
    LookAction setTarget(Player player);

    /**
     * Gets the player to look to.
     *
     * @return the player
     */
    Player getTarget();

    static LookAction create() {
        return Bukkit.getBotManager().newAction(LookAction.class);
    }
}
