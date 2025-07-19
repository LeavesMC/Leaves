package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.Bukkit;

/**
 * Represents an action for a bot to move to a specific direction.
 */
public interface MoveAction extends StateBotAction<MoveAction> {

    /**
     * Gets the direction of the move action.
     *
     * @return the direction of the move action
     */
    MoveDirection getDirection();

    /**
     * Sets the direction of the move action.
     *
     * @param direction the direction to set
     * @return this action instance
     */
    MoveAction setDirection(MoveDirection direction);

    /**
     * Represents possible movement directions for the bot.
     */
    enum MoveDirection {
        FORWARD("forward"),
        BACKWARD("backward"),
        LEFT("left"),
        RIGHT("right");

        public final String name;

        MoveDirection(String name) {
            this.name = name;
        }
    }

    static MoveAction create() {
        return Bukkit.getBotManager().newAction(MoveAction.class);
    }
}
