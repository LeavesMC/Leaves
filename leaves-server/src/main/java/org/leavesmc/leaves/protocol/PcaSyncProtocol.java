package org.leavesmc.leaves.protocol;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.plugin.MinecraftInternalPlugin;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.leavesmc.leaves.util.TagUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@LeavesProtocol.Register(namespace = "pca")
public class PcaSyncProtocol implements LeavesProtocol {

    public static final String PROTOCOL_ID = "pca";

    public static final ReentrantLock lock = new ReentrantLock(true);
    public static final ReentrantLock pairLock = new ReentrantLock(true);

    // send
    private static final Identifier ENABLE_PCA_SYNC_PROTOCOL = id("enable_pca_sync_protocol");
    private static final Identifier DISABLE_PCA_SYNC_PROTOCOL = id("disable_pca_sync_protocol");

    private static final Map<ServerPlayer, Pair<Identifier, BlockPos>> playerWatchBlockPos = new HashMap<>();
    private static final Map<ServerPlayer, Pair<Identifier, Entity>> playerWatchEntity = new HashMap<>();
    private static final Map<Pair<Identifier, BlockPos>, Set<ServerPlayer>> blockPosWatchPlayerSet = new HashMap<>();
    private static final Map<Pair<Identifier, Entity>, Set<ServerPlayer>> entityWatchPlayerSet = new HashMap<>();
    private static final MutablePair<Identifier, Entity> IdentifierEntityPair = new MutablePair<>();
    private static final MutablePair<Identifier, BlockPos> IdentifierBlockPosPair = new MutablePair<>();

    @Contract("_ -> new")
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(PROTOCOL_ID, path);
    }

    @ProtocolHandler.PlayerJoin
    private static void onJoin(ServerPlayer player) {
        if (LeavesConfig.protocol.pca.enable) {
            enablePcaSyncProtocol(player);
        }
    }

    @ProtocolHandler.BytebufReceiver(key = "cancel_sync_block_entity")
    private static void cancelSyncBlockEntityHandler(ServerPlayer player, FriendlyByteBuf buf) {
        PcaSyncProtocol.clearPlayerWatchBlock(player);
    }

    @ProtocolHandler.BytebufReceiver(key = "cancel_sync_entity")
    private static void cancelSyncEntityHandler(ServerPlayer player, FriendlyByteBuf buf) {
        PcaSyncProtocol.clearPlayerWatchEntity(player);
    }

    @ProtocolHandler.PayloadReceiver(payload = SyncBlockEntityPayload.class)
    private static void syncBlockEntityHandler(ServerPlayer player, SyncBlockEntityPayload payload) {
        BlockPos pos = payload.pos;
        ServerLevel world = player.level();

        Bukkit.getGlobalRegionScheduler().run(MinecraftInternalPlugin.INSTANCE, (task) -> {
            BlockState blockState = world.getBlockState(pos);
            clearPlayerWatchData(player);

            BlockEntity blockEntityAdj = null;
            if (blockState.getBlock() instanceof ChestBlock) {
                if (blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    BlockPos posAdj = pos.relative(ChestBlock.getConnectedDirection(blockState));
                    // The method in World now checks that the caller is from the same thread...
                    blockEntityAdj = world.getChunk(posAdj).getBlockEntity(posAdj);
                }
            }

            if (blockEntityAdj != null) {
                updateBlockEntity(player, blockEntityAdj);
            }

            // The method in World now checks that the caller is from the same thread...
            BlockEntity blockEntity = world.getChunk(pos).getBlockEntity(pos);
            if (blockEntity != null) {
                updateBlockEntity(player, blockEntity);
            }

            Pair<Identifier, BlockPos> pair = new ImmutablePair<>(player.level().dimension().identifier(), pos);
            lock.lock();
            playerWatchBlockPos.put(player, pair);
            if (!blockPosWatchPlayerSet.containsKey(pair)) {
                blockPosWatchPlayerSet.put(pair, new HashSet<>());
            }
            blockPosWatchPlayerSet.get(pair).add(player);
            lock.unlock();
        });
    }

    @ProtocolHandler.PayloadReceiver(payload = SyncEntityPayload.class)
    private static void syncEntityHandler(ServerPlayer player, SyncEntityPayload payload) {
        MinecraftServer server = MinecraftServer.getServer();
        int entityId = payload.entityId;
        ServerLevel world = player.level();

        Bukkit.getGlobalRegionScheduler().run(MinecraftInternalPlugin.INSTANCE, (task) -> {
            Entity entity = world.getEntity(entityId);

            if (entity != null) {
                clearPlayerWatchData(player);

                if (entity instanceof Player) {
                    switch (LeavesConfig.protocol.pca.syncPlayerEntity) {
                        case NOBODY -> {
                            return;
                        }
                        case BOT -> {
                            if (!(entity instanceof ServerBot)) {
                                return;
                            }
                        }
                        case OPS -> {
                            if (!(entity instanceof ServerBot) && !server.getPlayerList().isOp(player.nameAndId())) {
                                return;
                            }
                        }
                        case OPS_AND_SELF -> {
                            if (!(entity instanceof ServerBot) && !server.getPlayerList().isOp(player.nameAndId()) && entity != player) {
                                return;
                            }
                        }
                        case EVERYONE -> {
                        }
                        case null -> LeavesLogger.LOGGER.warning("pcaSyncPlayerEntity wtf???");
                    }
                }
                updateEntity(player, entity);

                Pair<Identifier, Entity> pair = new ImmutablePair<>(entity.level().dimension().identifier(), entity);
                lock.lock();
                playerWatchEntity.put(player, pair);
                if (!entityWatchPlayerSet.containsKey(pair)) {
                    entityWatchPlayerSet.put(pair, new HashSet<>());
                }
                entityWatchPlayerSet.get(pair).add(player);
                lock.unlock();
            }
        });
    }

    public static void onConfigModify(boolean enable) {
        if (enable) {
            enablePcaSyncProtocolGlobal();
        } else {
            disablePcaSyncProtocolGlobal();
        }
    }

    public static void enablePcaSyncProtocol(@NotNull ServerPlayer player) {
        ProtocolUtils.sendEmptyPacket(player, ENABLE_PCA_SYNC_PROTOCOL);
        lock.lock();
        lock.unlock();
    }

    public static void disablePcaSyncProtocol(@NotNull ServerPlayer player) {
        ProtocolUtils.sendEmptyPacket(player, DISABLE_PCA_SYNC_PROTOCOL);
    }

    public static void updateEntity(@NotNull ServerPlayer player, @NotNull Entity entity) {
        CompoundTag nbt = TagUtil.saveEntity(entity);
        ProtocolUtils.sendPayloadPacket(player, new UpdateEntityPayload(entity.level().dimension().identifier(), entity.getId(), nbt));
    }

    public static void updateBlockEntity(@NotNull ServerPlayer player, @NotNull BlockEntity blockEntity) {
        Level world = blockEntity.getLevel();

        if (world == null) {
            return;
        }

        ProtocolUtils.sendPayloadPacket(player, new UpdateBlockEntityPayload(world.dimension().identifier(), blockEntity.getBlockPos(), blockEntity.saveWithoutMetadata(world.registryAccess())));
    }

    private static MutablePair<Identifier, Entity> getIdentifierEntityPair(Identifier Identifier, Entity entity) {
        pairLock.lock();
        IdentifierEntityPair.setLeft(Identifier);
        IdentifierEntityPair.setRight(entity);
        pairLock.unlock();
        return IdentifierEntityPair;
    }

    private static MutablePair<Identifier, BlockPos> getIdentifierBlockPosPair(Identifier Identifier, BlockPos pos) {
        pairLock.lock();
        IdentifierBlockPosPair.setLeft(Identifier);
        IdentifierBlockPosPair.setRight(pos);
        pairLock.unlock();
        return IdentifierBlockPosPair;
    }

    private static @Nullable Set<ServerPlayer> getWatchPlayerList(@NotNull Entity entity) {
        return entityWatchPlayerSet.get(getIdentifierEntityPair(entity.level().dimension().identifier(), entity));
    }

    private static @Nullable Set<ServerPlayer> getWatchPlayerList(@NotNull Level world, @NotNull BlockPos blockPos) {
        return blockPosWatchPlayerSet.get(getIdentifierBlockPosPair(world.dimension().identifier(), blockPos));
    }

    public static boolean syncEntityToClient(@NotNull Entity entity) {
        if (entity.level().isClientSide()) {
            return false;
        }
        lock.lock();
        Set<ServerPlayer> playerList = getWatchPlayerList(entity);
        boolean ret = false;
        if (playerList != null) {
            for (ServerPlayer player : playerList) {
                updateEntity(player, entity);
                ret = true;
            }
        }
        lock.unlock();
        return ret;
    }

    public static boolean syncBlockEntityToClient(@NotNull BlockEntity blockEntity) {
        boolean ret = false;
        Level world = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        if (world != null) {
            if (world.isClientSide()) {
                return false;
            }
            BlockState blockState = world.getBlockState(pos);
            lock.lock();
            Set<ServerPlayer> playerList = getWatchPlayerList(world, blockEntity.getBlockPos());

            Set<ServerPlayer> playerListAdj = null;

            if (blockState.getBlock() instanceof ChestBlock) {
                if (blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    BlockPos posAdj = pos.relative(ChestBlock.getConnectedDirection(blockState));
                    playerListAdj = getWatchPlayerList(world, posAdj);
                }
            }
            if (playerListAdj != null) {
                if (playerList == null) {
                    playerList = playerListAdj;
                } else {
                    playerList.addAll(playerListAdj);
                }
            }

            if (playerList != null) {
                for (ServerPlayer player : playerList) {
                    updateBlockEntity(player, blockEntity);
                    ret = true;
                }
            }
            lock.unlock();
        }
        return ret;
    }

    private static void clearPlayerWatchEntity(ServerPlayer player) {
        lock.lock();
        Pair<Identifier, Entity> pair = playerWatchEntity.get(player);
        if (pair != null) {
            Set<ServerPlayer> playerSet = entityWatchPlayerSet.get(pair);
            playerSet.remove(player);
            if (playerSet.isEmpty()) {
                entityWatchPlayerSet.remove(pair);
            }
            playerWatchEntity.remove(player);
        }
        lock.unlock();
    }

    private static void clearPlayerWatchBlock(ServerPlayer player) {
        lock.lock();
        Pair<Identifier, BlockPos> pair = playerWatchBlockPos.get(player);
        if (pair != null) {
            Set<ServerPlayer> playerSet = blockPosWatchPlayerSet.get(pair);
            playerSet.remove(player);
            if (playerSet.isEmpty()) {
                blockPosWatchPlayerSet.remove(pair);
            }
            playerWatchBlockPos.remove(player);
        }
        lock.unlock();
    }

    public static void disablePcaSyncProtocolGlobal() {
        lock.lock();
        playerWatchBlockPos.clear();
        playerWatchEntity.clear();
        blockPosWatchPlayerSet.clear();
        entityWatchPlayerSet.clear();
        lock.unlock();
        for (ServerPlayer player : MinecraftServer.getServer().getPlayerList().getPlayers()) {
            disablePcaSyncProtocol(player);
        }
    }

    public static void enablePcaSyncProtocolGlobal() {
        for (ServerPlayer player : MinecraftServer.getServer().getPlayerList().getPlayers()) {
            enablePcaSyncProtocol(player);
        }
    }


    public static void clearPlayerWatchData(ServerPlayer player) {
        PcaSyncProtocol.clearPlayerWatchBlock(player);
        PcaSyncProtocol.clearPlayerWatchEntity(player);
    }

    @Override
    public boolean isActive() {
        return LeavesConfig.protocol.pca.enable;
    }

    public record UpdateEntityPayload(Identifier dimension, int entityId, CompoundTag tag) implements LeavesCustomPayload {

        @ID
        public static final Identifier UPDATE_ENTITY = PcaSyncProtocol.id("update_entity");

        @Codec
        public static final StreamCodec<FriendlyByteBuf, UpdateEntityPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            UpdateEntityPayload::dimension,
            ByteBufCodecs.INT,
            UpdateEntityPayload::entityId,
            ByteBufCodecs.COMPOUND_TAG,
            UpdateEntityPayload::tag,
            UpdateEntityPayload::new
        );
    }

    public record UpdateBlockEntityPayload(Identifier dimension, BlockPos blockPos, CompoundTag tag) implements LeavesCustomPayload {

        @ID
        private static final Identifier UPDATE_BLOCK_ENTITY = PcaSyncProtocol.id("update_block_entity");

        @Codec
        private static final StreamCodec<FriendlyByteBuf, UpdateBlockEntityPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            UpdateBlockEntityPayload::dimension,
            BlockPos.STREAM_CODEC,
            UpdateBlockEntityPayload::blockPos,
            ByteBufCodecs.COMPOUND_TAG,
            UpdateBlockEntityPayload::tag,
            UpdateBlockEntityPayload::new
        );
    }

    public record SyncBlockEntityPayload(BlockPos pos) implements LeavesCustomPayload {
        @ID
        public static final Identifier SYNC_BLOCK_ENTITY = PcaSyncProtocol.id("sync_block_entity");

        @Codec
        private static final StreamCodec<FriendlyByteBuf, SyncBlockEntityPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncBlockEntityPayload::pos, SyncBlockEntityPayload::new
        );
    }

    public record SyncEntityPayload(int entityId) implements LeavesCustomPayload {
        @ID
        public static final Identifier SYNC_ENTITY = PcaSyncProtocol.id("sync_entity");

        @Codec
        private static final StreamCodec<FriendlyByteBuf, SyncEntityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncEntityPayload::entityId, SyncEntityPayload::new
        );
    }
}