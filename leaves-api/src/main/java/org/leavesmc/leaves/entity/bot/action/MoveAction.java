package org.leavesmc.leaves.entity.bot.action;

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
}
