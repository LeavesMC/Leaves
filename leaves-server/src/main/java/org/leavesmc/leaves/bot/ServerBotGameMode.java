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
            BlockState iblockdata1 = block.playerWillDestroy(this.level, pos, iblockdata, this.player);
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
    public InteractionResult useItemOn(@NotNull ServerPlayer player, Level world, @NotNull ItemStack stack, @NotNull InteractionHand hand, BlockHitResult hitResult) {
        BlockPos blockposition = hitResult.getBlockPos();
        BlockState iblockdata = world.getBlockState(blockposition);
        InteractionResult enuminteractionresult = InteractionResult.PASS;

        if (!iblockdata.getBlock().isEnabled(world.enabledFeatures())) {
            return InteractionResult.FAIL;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.PASS;
        }

        this.firedInteract = true;
        this.interactResult = false;
        this.interactPosition = blockposition.immutable();
        this.interactHand = hand;
        this.interactItemStack = stack.copy();

        boolean flag = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
        boolean flag1 = player.isSecondaryUseActive() && flag;

        if (!flag1) {
            InteractionResult iteminteractionresult = iblockdata.useItemOn(player.getItemInHand(hand), world, player, hand, hitResult);

            if (iteminteractionresult.consumesAction()) {
                return iteminteractionresult;
            }

            if (iteminteractionresult == InteractionResult.PASS && hand == InteractionHand.MAIN_HAND) {
                enuminteractionresult = iblockdata.useWithoutItem(world, player, hitResult);
                if (enuminteractionresult.consumesAction()) {
                    return enuminteractionresult;
                }
            }
        }

        if (!stack.isEmpty() && enuminteractionresult != InteractionResult.SUCCESS && !this.interactResult) {
            UseOnContext itemactioncontext = new UseOnContext(player, hand, hitResult);
            return stack.useOn(itemactioncontext);
        }
        return enuminteractionresult;
    }

    @Override
    public void setLevel(@NotNull ServerLevel world) {
    }
}
