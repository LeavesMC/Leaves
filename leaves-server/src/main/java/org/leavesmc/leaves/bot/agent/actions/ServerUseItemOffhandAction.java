package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemOffhandAction;

import static org.leavesmc.leaves.bot.agent.actions.ServerUseItemAction.useItem;

public class ServerUseItemOffhandAction extends AbstractUseBotAction<ServerUseItemOffhandAction> {

    public ServerUseItemOffhandAction() {
        super("use_offhand");
    }

    @Override
    protected boolean interact(@NotNull ServerBot bot) {
        return useItem(bot, InteractionHand.OFF_HAND).consumesAction();
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemOffhandAction(this);
    }
}
