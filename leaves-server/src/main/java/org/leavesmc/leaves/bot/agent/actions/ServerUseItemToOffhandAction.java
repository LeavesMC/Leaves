package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemToOffhandAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.Collections;

public class ServerUseItemToOffhandAction extends ServerTimerBotAction<ServerUseItemToOffhandAction> {
    private int useTick = -1;
    private int tickToRelease = -1;

    public ServerUseItemToOffhandAction() {
        super("use_to_offhand", CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), ServerUseItemToOffhandAction::new);
        this.setSuggestion(3, Pair.of(Collections.singletonList("-1"), "[UseTick]"));
    }

    @Override
    public void init() {
        super.init();
        syncTickToRelease();
    }

    @Override
    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) {
        super.loadCommand(player, result);
        this.useTick = result.readInt(-1);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        tickToRelease--;
        if (tickToRelease >= 0) {
            Entity entity = bot.getTargetEntity(3, null);
            boolean result = execute(bot, entity);
            if (useTick >=0) {
                return false;
            } else {
                return result;
            }
        } else {
            syncTickToRelease();
            bot.releaseUsingItem();
            return true;
        }
    }

    private void syncTickToRelease() {
        if (this.useTick >= 0) {
            this.tickToRelease = this.useTick;
        } else {
            this.tickToRelease = Integer.MAX_VALUE;
        }
    }

    public int getUseTick() {
        return useTick;
    }

    public void setUseTick(int useTick) {
        this.useTick = useTick;
    }

    public static boolean execute(ServerBot bot, Entity entity) {
        if (entity == null) {
            return false;
        }

        boolean flag = bot.interactOn(entity, InteractionHand.OFF_HAND).consumesAction();
        if (flag) {
            bot.swing(InteractionHand.OFF_HAND);
            bot.updateItemInHand(InteractionHand.OFF_HAND);
        }
        return flag;
    }

    @Override
    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        super.stop(bot, reason);
        bot.completeUsingItem();
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemToOffhandAction(this);
    }
}
