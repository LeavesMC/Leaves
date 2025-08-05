package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.EntityHitResult;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemToAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.List;

public class ServerUseItemToAction extends ServerTimerBotAction<ServerUseItemToAction> {
    private int useTick = -1;
    private int tickToRelease = -1;

    public ServerUseItemToAction() {
        super("use_to", CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), ServerUseItemToAction::new);
        this.setSuggestion(3, Pair.of(List.of("-1"), "[UseTick]"));
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
            EntityHitResult hitResult = bot.getEntityHitResult(3, null);
            boolean result = execute(bot, hitResult).consumesAction();
            if (useTick >= 0) {
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

    public static InteractionResult execute(ServerBot bot, EntityHitResult hitResult) {
        if (hitResult == null) {
            return InteractionResult.FAIL;
        }

        InteractionResult result;
        if (hitResult.getEntity() instanceof ArmorStand armorStand) {
            result = armorStand.interactAt(bot, hitResult.getLocation().subtract(armorStand.position()), InteractionHand.MAIN_HAND);
        } else {
            result = bot.interactOn(hitResult.getEntity(), InteractionHand.MAIN_HAND);
        }
        if (result.consumesAction()) {
            bot.swing(InteractionHand.MAIN_HAND);
            bot.updateItemInHand(InteractionHand.MAIN_HAND);
        }
        return result;
    }

    @Override
    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        super.stop(bot, reason);
        bot.completeUsingItem();
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemToAction(this);
    }
}
