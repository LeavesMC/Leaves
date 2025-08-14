package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerRotationAction;
import org.leavesmc.leaves.entity.bot.action.RotationAction;

public class CraftRotationAction extends CraftBotAction<RotationAction, ServerRotationAction> implements RotationAction {

    public CraftRotationAction(ServerRotationAction serverAction) {
        super(serverAction, CraftRotationAction::new);
    }

    @Override
    public RotationAction setYaw(float yaw) {
        serverAction.setYaw(yaw);
        return this;
    }

    @Override
    public RotationAction setPitch(float pitch) {
        serverAction.setPitch(pitch);
        return this;
    }

    @Override
    public float getYaw() {
        return serverAction.getYaw();
    }

    @Override
    public float getPitch() {
        return serverAction.getPitch();
    }
}
