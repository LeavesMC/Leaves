package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.ServerMountAction;
import org.leavesmc.leaves.entity.bot.action.MountAction;

public class CraftMountAction extends CraftBotAction<MountAction, ServerMountAction> implements MountAction {

    public CraftMountAction(ServerMountAction serverAction) {
        super(serverAction, CraftMountAction::new);
    }
}
