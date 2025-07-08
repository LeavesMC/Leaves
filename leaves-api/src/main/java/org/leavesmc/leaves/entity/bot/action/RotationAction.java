package org.leavesmc.leaves.entity.bot.action;

public interface RotationAction extends BotAction<RotationAction> {
    /**
     * Sets the yaw of the rotation.
     *
     * @param yaw the yaw to set
     * @return this action instance
     */
    RotationAction setYaw(float yaw);

    /**
     * Sets the pitch of the rotation.
     *
     * @param pitch the pitch to set
     * @return this action instance
     */
    RotationAction setPitch(float pitch);

    /**
     * Gets the yaw of the rotation.
     *
     * @return the yaw
     */
    float getYaw();

    /**
     * Gets the pitch of the rotation.
     *
     * @return the pitch
     */
    float getPitch();
}
