package org.leavesmc.leaves.protocol.jade.accessor;

import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.protocol.jade.payload.RequestBlockPayload;
import org.leavesmc.leaves.protocol.jade.payload.ServerPayloadContext;
import org.leavesmc.leaves.protocol.jade.provider.IServerDataProvider;
import org.leavesmc.leaves.protocol.jade.util.CommonUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Class to get information of block target and context.
 */
public class BlockAccessorImpl extends AccessorImpl<BlockHitResult> implements BlockAccessor {

    private final BlockState blockState;
    @Nullable
    private final Supplier<BlockEntity> blockEntity;
    private ItemStack fakeBlock;

    private BlockAccessorImpl(Builder builder) {
        super(builder.level, builder.player, builder.serverData, Suppliers.ofInstance(builder.hit), builder.connected, builder.showDetails);
        blockState = builder.blockState;
        blockEntity = builder.blockEntity;
        fakeBlock = builder.fakeBlock;
    }

    public static void handleRequest(RequestBlockPayload message, ServerPayloadContext context, Consumer<CompoundTag> responseSender) {
        ServerPlayer player = context.player();
        context.execute(() -> {
            BlockAccessor accessor = message.data().unpack(player);
            if (accessor == null) {
                return;
            }
            BlockPos pos = accessor.getPosition();
            ServerLevel world = player.serverLevel();
            double maxDistance = Mth.square(player.blockInteractionRange() + 21);
            if (pos.distSqr(player.blockPosition()) > maxDistance || !world.isLoaded(pos)) {
                return;
            }

            List<IServerDataProvider<BlockAccessor>> providers = CommonUtil.getBlockNBTProviders(accessor.getBlock(), accessor.getBlockEntity());
            CompoundTag tag = accessor.getServerData();
            for (IServerDataProvider<BlockAccessor> provider : providers) {
                if (!message.dataProviders().contains(provider)) {
                    continue;
                }
                try {
                    provider.appendServerData(tag, accessor);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            tag.putString("BlockId", CommonUtil.getId(accessor.getBlock()).toString());
            responseSender.accept(tag);
        });
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    @Override
    public BlockState getBlockState() {
        return blockState;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return blockEntity == null ? null : blockEntity.get();
    }

    @Override
    public BlockPos getPosition() {
        return getHitResult().getBlockPos();
    }

    @Override
    public Direction getSide() {
        return getHitResult().getDirection();
    }

    @Nullable
    @Override
    public Object getTarget() {
        return getBlockEntity();
    }

    @Override
    public ItemStack getFakeBlock() {
        return fakeBlock;
    }

    public static class Builder implements BlockAccessor.Builder {

        private Level level;
        private Player player;
        private CompoundTag serverData;
        private boolean connected;
        private boolean showDetails;
        private BlockHitResult hit;
        private BlockState blockState = Blocks.AIR.defaultBlockState();
        private Supplier<BlockEntity> blockEntity;
        private ItemStack fakeBlock = ItemStack.EMPTY;
        private boolean verify;

        @Override
        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        @Override
        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        @Override
        public Builder showDetails(boolean showDetails) {
            this.showDetails = showDetails;
            return this;
        }

        @Override
        public Builder hit(BlockHitResult hit) {
            this.hit = hit;
            return this;
        }

        @Override
        public Builder blockState(BlockState blockState) {
            this.blockState = blockState;
            return this;
        }

        @Override
        public Builder blockEntity(Supplier<BlockEntity> blockEntity) {
            this.blockEntity = blockEntity;
            return this;
        }

        @Override
        public Builder fakeBlock(ItemStack stack) {
            fakeBlock = stack;
            return this;
        }

        @Override
        public Builder from(BlockAccessor accessor) {
            level = accessor.getLevel();
            player = accessor.getPlayer();
            serverData = accessor.getServerData();
            connected = accessor.isServerConnected();
            showDetails = accessor.showDetails();
            hit = accessor.getHitResult();
            blockEntity = accessor::getBlockEntity;
            blockState = accessor.getBlockState();
            fakeBlock = accessor.getFakeBlock();
            return this;
        }

        @Override
        public BlockAccessor build() {
            BlockAccessorImpl accessor = new BlockAccessorImpl(this);
            if (verify) {
                accessor.requireVerification();
            }
            return accessor;
        }
    }

    public record SyncData(boolean showDetails, BlockHitResult hit, BlockState blockState, ItemStack fakeBlock) {
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                SyncData::showDetails,
                StreamCodec.of(FriendlyByteBuf::writeBlockHitResult, FriendlyByteBuf::readBlockHitResult),
                SyncData::hit,
                ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY),
                SyncData::blockState,
                ItemStack.OPTIONAL_STREAM_CODEC,
                SyncData::fakeBlock,
                SyncData::new
        );

        public BlockAccessor unpack(ServerPlayer player) {
            Supplier<BlockEntity> blockEntity = null;
            if (blockState.hasBlockEntity()) {
                blockEntity = Suppliers.memoize(() -> player.level().getBlockEntity(hit.getBlockPos()));
            }
            return new Builder()
                    .level(player.level())
                    .player(player)
                    .showDetails(showDetails)
                    .hit(hit)
                    .blockState(blockState)
                    .blockEntity(blockEntity)
                    .fakeBlock(fakeBlock)
                    .build();
        }
    }
}