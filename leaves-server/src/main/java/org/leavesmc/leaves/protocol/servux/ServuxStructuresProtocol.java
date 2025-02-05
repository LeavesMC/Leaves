package org.leavesmc.leaves.protocol.servux;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Powered by Servux(https://github.com/sakura-ryoko/servux)

@LeavesProtocol(namespace = "servux")
public class ServuxStructuresProtocol {

    public static final int PROTOCOL_VERSION = 2;

    private static final int updateInterval = 40;
    private static final int timeout = 30 * 20;

    public static boolean refreshSpawnMetadata = false;
    private static int retainDistance;

    public static final ResourceLocation CHANNEL = ServuxProtocol.id("structures");

    private static final Map<Integer, ServerPlayer> players = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<ChunkPos, Timeout>> timeouts = new HashMap<>();

    @ProtocolHandler.PayloadReceiver(payload = StructuresPayload.class, payloadId = "structures")
    public static void onPacketReceive(ServerPlayer player, StructuresPayload payload) {
        if (!LeavesConfig.protocol.servux.structureProtocol) {
            return;
        }

        switch (payload.packetType()) {
            case PACKET_C2S_STRUCTURES_REGISTER -> onPlayerSubscribed(player);
            case PACKET_C2S_REQUEST_SPAWN_METADATA -> refreshSpawnMetadata(player);
            case PACKET_C2S_STRUCTURES_UNREGISTER -> {
                onPlayerLoggedOut(player);
                refreshSpawnMetadata(player);
            }
        }
    }

    @ProtocolHandler.PlayerJoin
    public static void onPlayerLoggedIn(ServerPlayer player) {
        if (!LeavesConfig.protocol.servux.structureProtocol) {
            return;
        }

        onPlayerSubscribed(player);
    }

    @ProtocolHandler.PlayerLeave
    public static void onPlayerLoggedOut(@NotNull ServerPlayer player) {
        if (!LeavesConfig.protocol.servux.structureProtocol) {
            return;
        }

        players.remove(player.getId());
    }

    @ProtocolHandler.Ticker
    public static void tick() {
        if (!LeavesConfig.protocol.servux.structureProtocol) {
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        int tickCounter = server.getTickCount();
        if ((tickCounter % updateInterval) == 0) {
            retainDistance = server.getPlayerList().getViewDistance() + 2;
            for (ServerPlayer player : players.values()) {
                if (refreshSpawnMetadata) {
                    refreshSpawnMetadata(player);
                }

                // TODO DimensionChange
                refreshTrackedChunks(player, tickCounter);
            }

            if (refreshSpawnMetadata) {
                refreshSpawnMetadata = false;
            }
        }
    }

    public static void onStartedWatchingChunk(ServerPlayer player, LevelChunk chunk) {
        if (!LeavesConfig.protocol.servux.structureProtocol) {
            return;
        }

        MinecraftServer server = player.getServer();

        if (players.containsKey(player.getId()) && server != null) {
            addChunkTimeoutIfHasReferences(player.getUUID(), chunk, server.getTickCount());
        }
    }

    private static void addChunkTimeoutIfHasReferences(final UUID uuid, LevelChunk chunk, final int tickCounter) {
        final ChunkPos pos = chunk.getPos();

        if (chunkHasStructureReferences(pos.x, pos.z, chunk.getLevel())) {
            final Map<ChunkPos, Timeout> map = timeouts.computeIfAbsent(uuid, (u) -> new HashMap<>());
            map.computeIfAbsent(pos, (p) -> new Timeout(tickCounter - timeout));
        }
    }

    private static boolean chunkHasStructureReferences(int chunkX, int chunkZ, Level world) {
        if (!world.hasChunk(chunkX, chunkZ)) {
            return false;
        }

        ChunkAccess chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_STARTS, false);

        for (Map.Entry<Structure, LongSet> entry : chunk.getAllReferences().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                return true;
            }
        }

        return false;
    }


    public static void onPlayerSubscribed(@NotNull ServerPlayer player) {
        if (!players.containsKey(player.getId())) {
            players.put(player.getId(), player);
        } else {
            LeavesLogger.LOGGER.warning(player.getScoreboardName() + " re-register servux:structures");
        }

        CompoundTag tag = new CompoundTag();
        tag.putString("name", "structure_bounding_boxes");
        tag.putString("id", CHANNEL.toString());
        tag.putInt("version", PROTOCOL_VERSION);
        tag.putString("servux", ServuxProtocol.SERVUX_STRING);
        tag.putInt("timeout", timeout);

        MinecraftServer server = MinecraftServer.getServer();
        BlockPos spawnPos = server.overworld().levelData.getSpawnPos();
        tag.putInt("spawnPosX", spawnPos.getX());
        tag.putInt("spawnPosY", spawnPos.getY());
        tag.putInt("spawnPosZ", spawnPos.getZ());
        tag.putInt("spawnChunkRadius", server.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS));

        sendPacket(player, new StructuresPayload(StructuresPayloadType.PACKET_S2C_METADATA, tag));
        initialSyncStructures(player, player.moonrise$getViewDistanceHolder().getViewDistances().sendViewDistance() + 2, server.getTickCount());
    }

    public static void refreshSpawnMetadata(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", CHANNEL.toString());
        tag.putString("servux", ServuxProtocol.SERVUX_STRING);

        MinecraftServer server = MinecraftServer.getServer();
        BlockPos spawnPos = server.overworld().levelData.getSpawnPos();
        tag.putInt("spawnPosX", spawnPos.getX());
        tag.putInt("spawnPosY", spawnPos.getY());
        tag.putInt("spawnPosZ", spawnPos.getZ());
        tag.putInt("spawnChunkRadius", server.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS));

        sendPacket(player, new StructuresPayload(StructuresPayloadType.PACKET_S2C_SPAWN_METADATA, tag));
    }

    public static void initialSyncStructures(ServerPlayer player, int chunkRadius, int tickCounter) {
        UUID uuid = player.getUUID();
        ChunkPos center = player.getLastSectionPos().chunk();
        Map<Structure, LongSet> references = getStructureReferences(player.serverLevel(), center, chunkRadius);

        timeouts.remove(uuid);

        sendStructures(player, references, tickCounter);
    }

    public static Map<Structure, LongSet> getStructureReferences(ServerLevel world, ChunkPos center, int chunkRadius) {
        Map<Structure, LongSet> references = new HashMap<>();

        for (int cx = center.x - chunkRadius; cx <= center.x + chunkRadius; ++cx) {
            for (int cz = center.z - chunkRadius; cz <= center.z + chunkRadius; ++cz) {
                getReferencesFromChunk(cx, cz, world, references);
            }
        }

        return references;
    }

    public static void getReferencesFromChunk(int chunkX, int chunkZ, Level world, Map<Structure, LongSet> references) {
        if (!world.hasChunk(chunkX, chunkZ)) {
            return;
        }

        ChunkAccess chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_STARTS, false);

        for (Map.Entry<Structure, LongSet> entry : chunk.getAllReferences().entrySet()) {
            Structure feature = entry.getKey();
            LongSet startChunks = entry.getValue();

            // TODO add an option && feature != StructureFeature.MINESHAFT (?)
            if (!startChunks.isEmpty()) {
                references.merge(feature, startChunks, (oldSet, entrySet) -> {
                    LongOpenHashSet newSet = new LongOpenHashSet(oldSet);
                    newSet.addAll(entrySet);
                    return newSet;
                });
            }
        }
    }

    public static void sendStructures(ServerPlayer player, Map<Structure, LongSet> references, int tickCounter) {
        ServerLevel world = player.serverLevel();
        Map<ChunkPos, StructureStart> starts = getStructureStarts(world, references);

        if (!starts.isEmpty()) {
            addOrRefreshTimeouts(player.getUUID(), references, tickCounter);

            ListTag structureList = getStructureList(starts, world);

            if (players.containsKey(player.getId())) {
                CompoundTag test = new CompoundTag();
                test.put("Structures", structureList.copy());
                sendPacket(player, new StructuresPayload(StructuresPayloadType.PACKET_S2C_STRUCTURE_DATA_START, test));
            }
        }
    }

    public static ListTag getStructureList(Map<ChunkPos, StructureStart> structures, ServerLevel world) {
        ListTag list = new ListTag();
        StructurePieceSerializationContext ctx = StructurePieceSerializationContext.fromLevel(world);

        for (Map.Entry<ChunkPos, StructureStart> entry : structures.entrySet()) {
            ChunkPos pos = entry.getKey();
            list.add(entry.getValue().createTag(ctx, pos));
        }

        return list;
    }

    public static Map<ChunkPos, StructureStart> getStructureStarts(ServerLevel world, Map<Structure, LongSet> references) {
        Map<ChunkPos, StructureStart> starts = new HashMap<>();

        for (Map.Entry<Structure, LongSet> entry : references.entrySet()) {
            Structure structure = entry.getKey();
            LongSet startChunks = entry.getValue();
            LongIterator iter = startChunks.iterator();

            while (iter.hasNext()) {
                ChunkPos pos = new ChunkPos(iter.nextLong());

                if (!world.hasChunk(pos.x, pos.z)) {
                    continue;
                }

                ChunkAccess chunk = world.getChunk(pos.x, pos.z, ChunkStatus.STRUCTURE_STARTS, false);
                StructureStart start = chunk.getStartForStructure(structure);

                if (start != null) {
                    starts.put(pos, start);
                }
            }
        }

        return starts;
    }

    public static void refreshTrackedChunks(ServerPlayer player, int tickCounter) {
        UUID uuid = player.getUUID();
        Map<ChunkPos, Timeout> map = timeouts.get(uuid);

        if (map != null) {
            sendAndRefreshExpiredStructures(player, map, tickCounter);
        }
    }

    public static void sendAndRefreshExpiredStructures(ServerPlayer player, Map<ChunkPos, Timeout> map, int tickCounter) {
        Set<ChunkPos> positionsToUpdate = new HashSet<>();

        for (Map.Entry<ChunkPos, Timeout> entry : map.entrySet()) {
            Timeout out = entry.getValue();

            if (out.needsUpdate(tickCounter, timeout)) {
                positionsToUpdate.add(entry.getKey());
            }
        }

        if (!positionsToUpdate.isEmpty()) {
            ServerLevel world = player.serverLevel();
            ChunkPos center = player.getLastSectionPos().chunk();
            Map<Structure, LongSet> references = new HashMap<>();

            for (ChunkPos pos : positionsToUpdate) {
                if (isOutOfRange(pos, center)) {
                    map.remove(pos);
                } else {
                    getReferencesFromChunk(pos.x, pos.z, world, references);

                    Timeout timeout = map.get(pos);

                    if (timeout != null) {
                        timeout.setLastSync(tickCounter);
                    }
                }
            }

            if (!references.isEmpty()) {
                sendStructures(player, references, tickCounter);
            }
        }
    }

    protected static boolean isOutOfRange(ChunkPos pos, ChunkPos center) {
        return Math.abs(pos.x - center.x) > retainDistance || Math.abs(pos.z - center.z) > retainDistance;
    }

    public static void addOrRefreshTimeouts(final UUID uuid, final Map<Structure, LongSet> references, final int tickCounter) {
        Map<ChunkPos, Timeout> map = timeouts.computeIfAbsent(uuid, (u) -> new HashMap<>());

        for (LongSet chunks : references.values()) {
            for (Long chunkPosLong : chunks) {
                final ChunkPos pos = new ChunkPos(chunkPosLong);
                map.computeIfAbsent(pos, (p) -> new Timeout(tickCounter)).setLastSync(tickCounter);
            }
        }
    }

    public enum StructuresPayloadType {
        PACKET_S2C_METADATA(1),
        PACKET_S2C_STRUCTURE_DATA(2),
        PACKET_C2S_STRUCTURES_REGISTER(3),
        PACKET_C2S_STRUCTURES_UNREGISTER(4),
        PACKET_S2C_STRUCTURE_DATA_START(5),
        PACKET_S2C_SPAWN_METADATA(10),
        PACKET_C2S_REQUEST_SPAWN_METADATA(11);

        private static final class Helper {
            static Map<Integer, StructuresPayloadType> ID_TO_TYPE = new HashMap<>();
        }

        public final int type;

        StructuresPayloadType(int type) {
            this.type = type;
            Helper.ID_TO_TYPE.put(type, this);
        }

        public static StructuresPayloadType fromId(int id) {
            return Helper.ID_TO_TYPE.get(id);
        }
    }

    public record StructuresPayload(StructuresPayloadType packetType, CompoundTag nbt, FriendlyByteBuf buffer) implements LeavesCustomPayload<StructuresPayload> {

        public StructuresPayload(StructuresPayloadType packetType, CompoundTag nbt) {
            this(packetType, nbt, null);
        }

        public StructuresPayload(StructuresPayloadType packetType, FriendlyByteBuf buffer) {
            this(packetType, new CompoundTag(), buffer);
        }

        @New
        private static StructuresPayload decode(ResourceLocation id, FriendlyByteBuf buf) {
            int i = buf.readVarInt();
            StructuresPayloadType type = StructuresPayloadType.fromId(i);

            if (type == null) {
                throw new IllegalStateException("invalid packet type received");
            } else if (type.equals(StructuresPayloadType.PACKET_S2C_STRUCTURE_DATA)) {
                return new StructuresPayload(type, new FriendlyByteBuf(buf.readBytes(buf.readableBytes())));
            } else {
                return new StructuresPayload(type, buf.readNbt());
            }
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(this.packetType.type);
            if (this.packetType.equals(StructuresPayloadType.PACKET_S2C_STRUCTURE_DATA)) {
                buf.writeBytes(this.buffer.readBytes(this.buffer.readableBytes()));
            } else {
                buf.writeNbt(this.nbt);
            }
        }

        @Override
        public ResourceLocation id() {
            return CHANNEL;
        }
    }

    public static class Timeout {
        private int lastSync;

        public Timeout(int currentTick) {
            this.lastSync = currentTick;
        }

        public boolean needsUpdate(int currentTick, int timeout) {
            return currentTick - this.lastSync >= timeout;
        }

        public void setLastSync(int tickCounter) {
            this.lastSync = tickCounter;
        }
    }

    public static void sendPacket(ServerPlayer player, StructuresPayload payload) {
        if (!LeavesConfig.protocol.servux.structureProtocol) {
            return;
        }

        if (payload.packetType() == StructuresPayloadType.PACKET_S2C_STRUCTURE_DATA_START) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeNbt(payload.nbt());
            PacketSplitter.send(ServuxStructuresProtocol::sendWithSplitter, buffer, player);
        } else {
            ProtocolUtils.sendPayloadPacket(player, payload);
        }
    }

    private static void sendWithSplitter(ServerPlayer player, FriendlyByteBuf buf) {
        sendPacket(player, new StructuresPayload(StructuresPayloadType.PACKET_S2C_STRUCTURE_DATA, buf));
    }
}
