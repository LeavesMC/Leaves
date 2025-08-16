package org.leavesmc.leaves.bot;

import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerBotGameMode extends ServerPlayerGameMode {

    public ServerBotGameMode(ServerBot bot) {
        super(bot);
        super.setGameModeForPlayer(GameType.SURVIVAL, null);
    }

    @Override
    public boolean changeGameModeForPlayer(@NotNull GameType gameMode) {
        return false;
    }

    @Nullable
    @Override
    public PlayerGameModeChangeEvent changeGameModeForPlayer(@NotNull GameType gameMode, PlayerGameModeChangeEvent.@NotNull Cause cause, @Nullable Component cancelMessage) {
        return null;
    }

    @Override
    protected void setGameModeForPlayer(@NotNull GameType gameMode, @Nullable GameType previousGameMode) {
    }

    @Override
    public void tick() {
    }

    @Override
    public void destroyAndAck(@NotNull BlockPos pos, int sequence, @NotNull String reason) {
        this.destroyBlock(pos);
    }

    @Override
    public boolean destroyBlock(@NotNull BlockPos pos) {
        BlockState iblockdata = this.level.getBlockState(pos);
        BlockEntity tileentity = this.level.getBlockEntity(pos);
        Block block = iblockdata.getBlock();

        if (this.player.blockActionRestricted(this.level, pos, this.getGameModeForPlayer())) {
            return false;
        } else {
            this.level.captureDrops = null;
            BlockState iblockdata1 = org.leavesmc.leaves.command.subcommands.BlockUpdateCommand.isNoBlockUpdate() ? iblockdata : block.playerWillDestroy(this.level, pos, iblockdata, this.player); // Leaves - no block update
            boolean flag = this.level.removeBlock(pos, false);

            if (flag) {
                block.destroy(this.level, pos, iblockdata1);
            }

            ItemStack itemstack = this.player.getMainHandItem();
            ItemStack itemstack1 = itemstack.copy();

            boolean flag1 = this.player.hasCorrectToolForDrops(iblockdata1);

            itemstack.mineBlock(this.level, iblockdata1, pos, this.player);
            if (flag && flag1) {
                Block.dropResources(iblockdata1, this.level, pos, tileentity, this.player, itemstack1, true);
            }

            if (flag) {
                iblockdata.getBlock().popExperience(this.level, pos, block.getExpDrop(iblockdata, this.level, pos, itemstack, true), this.player);
            }

            return true;
        }
    }

    @NotNull
    @Override
    public InteractionResult useItemOn(@NotNull ServerPlayer player, Level level, @NotNull ItemStack stack, @NotNull InteractionHand hand, BlockHitResult hitResult) {
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);

        if (!blockState.getBlock().isEnabled(level.enabledFeatures())) {
            return InteractionResult.FAIL;
        }

        boolean flag = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
        boolean flag1 = player.isSecondaryUseActive() && flag;

        if (!flag1) {
            InteractionResult iteminteractionresult = blockState.useItemOn(player.getItemInHand(hand), level, player, hand, hitResult);

            if (iteminteractionresult.consumesAction()) {
                return iteminteractionresult;
            }

            if (iteminteractionresult instanceof InteractionResult.TryEmptyHandInteraction && hand == InteractionHand.MAIN_HAND) {
                InteractionResult interactionResult = blockState.useWithoutItem(level, player, hitResult);
                if (interactionResult.consumesAction()) {
                    return interactionResult;
                }
            }
        }

        if (!stack.isEmpty() && !player.getCooldowns().isOnCooldown(stack)) {
            UseOnContext itemactioncontext = new UseOnContext(player, hand, hitResult);
            return stack.useOn(itemactioncontext);
        } else {
            return InteractionResult.PASS;
        }
    }
}
