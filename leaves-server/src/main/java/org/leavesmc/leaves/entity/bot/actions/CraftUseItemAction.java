package org.leavesmc.leaves.entity.bot.actions;

import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.actions.ServerUseItemAction;
import org.leavesmc.leaves.entity.bot.action.UseItemAction;

public class CraftUseItemAction extends CraftTimerBotAction<UseItemAction, ServerUseItemAction> implements UseItemAction {

    public CraftUseItemAction(ServerUseItemAction serverAction) {
        super(serverAction, CraftUseItemAction::new);
    }

    public boolean doTick(@NotNull ServerBot bot) {
        return serverAction.doTick(bot);
    }

    @Override
    public int getUseTick() {
        return serverAction.getUseTick();
    }

    @Override
    public CraftUseItemAction setUseTick(int useTick) {
        serverAction.setUseTick(useTick);
        return this;
    }
}
