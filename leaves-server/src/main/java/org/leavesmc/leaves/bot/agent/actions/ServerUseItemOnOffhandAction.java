package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemOnOffhandAction;

import static org.leavesmc.leaves.bot.agent.actions.ServerUseItemOnAction.useItemOn;

public class ServerUseItemOnOffhandAction extends ServerUseBotAction<ServerUseItemOnOffhandAction> {

    public ServerUseItemOnOffhandAction() {
        super("use_on_offhand", ServerUseItemOnOffhandAction::new);
    }

    @Override
    protected boolean interact(@NotNull ServerBot bot) {
        BlockHitResult hitResult = bot.getBlockHitResult();
        return useItemOn(bot, hitResult, InteractionHand.OFF_HAND).consumesAction();
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemOnOffhandAction(this);
    }
}
