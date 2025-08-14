package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

public class CraftMountAction extends CraftBotAction<MountAction, ServerMountAction> implements MountAction {

    public CraftMountAction(ServerMountAction serverAction) {
        super(serverAction, CraftMountAction::new);
    }
}
