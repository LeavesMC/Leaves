package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

public class CraftSneakAction extends CraftBotAction<SneakAction, ServerSneakAction> implements SneakAction {

    public CraftSneakAction(ServerSneakAction serverAction) {
        super(serverAction, CraftSneakAction::new);
    }
}
