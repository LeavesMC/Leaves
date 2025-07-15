package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.actions.CraftUseItemOnAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;
import org.leavesmc.leaves.plugin.MinecraftInternalPlugin;

import java.util.Collections;

public class ServerUseItemOnAction extends ServerTimerBotAction<ServerUseItemOnAction> {
    private int useTick = -1;
    private int tickToRelease = -1;

    public ServerUseItemOnAction() {
        super("use_on", CommandArgument.of(CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER, CommandArgumentType.INTEGER), ServerUseItemOnAction::new);
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
            HitResult hitResult = bot.getRayTrace(5, ClipContext.Fluid.NONE);
            boolean result = execute(bot, hitResult);
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

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putInt("useTick", this.useTick);
        nbt.putInt("tickToRelease", this.tickToRelease);
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.useTick = nbt.getInt("useTick").orElseThrow();
        this.tickToRelease = nbt.getInt("tickToRelease").orElseThrow();
    }

    public int getUseTick() {
        return useTick;
    }

    public void setUseTick(int useTick) {
        this.useTick = useTick;
    }

    public static boolean execute(@NotNull ServerBot bot, HitResult result) {
        if (!(result instanceof BlockHitResult blockHitResult)) {
            return false;
        }
        BlockState state = bot.level().getBlockState(blockHitResult.getBlockPos());
        if (state.isAir()) {
            return false;
        } else {
            bot.swing(InteractionHand.MAIN_HAND);
            if (state.getBlock() == Blocks.TRAPPED_CHEST) {
                BlockEntity entity = bot.level().getBlockEntity(blockHitResult.getBlockPos());
                if (entity instanceof TrappedChestBlockEntity chestBlockEntity) {
                    chestBlockEntity.startOpen(bot);
                    Bukkit.getScheduler().runTaskLater(MinecraftInternalPlugin.INSTANCE, () -> chestBlockEntity.stopOpen(bot), 1);
                    return true;
                } else {
                    return false;
                }

            } else {
                bot.updateItemInHand(InteractionHand.MAIN_HAND);
                return bot.gameMode.useItemOn(bot, bot.level(), bot.getItemInHand(InteractionHand.MAIN_HAND), InteractionHand.MAIN_HAND, blockHitResult).consumesAction();
            }
        }
    }

    @Override
    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        super.stop(bot, reason);
        bot.completeUsingItem();
    }

    @Override
    public Object asCraft() {
        return new CraftUseItemOnAction(this);
    }
}
