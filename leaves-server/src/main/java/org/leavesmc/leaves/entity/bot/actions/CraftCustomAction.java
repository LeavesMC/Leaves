package org.leavesmc.leaves.entity.bot.actions;

import org.leavesmc.leaves.bot.agent.actions.custom.ServerCustomAction;
import org.leavesmc.leaves.entity.bot.action.custom.CustomAction;

public class CraftCustomAction extends CraftBotAction<CustomAction, ServerCustomAction> implements  CustomAction {

    public CraftCustomAction(ServerCustomAction serverAction) {
        super(serverAction, CraftCustomAction::new);
    }

    @Override
    public <T> T getMeta(String key) {
        return serverAction.getMeta(key);
    }

    @Override
    public <T> void setMeta(String key, T meta) {
        serverAction.setMeta(key, meta);
    }
}
