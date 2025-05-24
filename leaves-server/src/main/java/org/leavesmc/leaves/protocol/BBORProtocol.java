package org.leavesmc.leaves.protocol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@LeavesProtocol.Register(namespace = "bbor")
public class BBORProtocol implements LeavesProtocol {

    public static final String PROTOCOL_ID = "bbor";

    // send
    private static final ResourceLocation INITIALIZE_CLIENT = id("initialize");
    private static final ResourceLocation ADD_BOUNDING_BOX = id("add_bounding_box_v2");
    private static final ResourceLocation STRUCTURE_LIST_SYNC = id("structure_list_sync_v1");
    // call
    private static final Map<Integer, ServerPlayer> players = new ConcurrentHashMap<>();
    private static final Map<Integer, Set<BBoundingBox>> playerBoundingBoxesCache = new HashMap<>();
    private static final Map<ResourceLocation, Map<BBoundingBox, Set<BBoundingBox>>> dimensionCache = new ConcurrentHashMap<>();

    private static boolean initialized = false;

    @Contract("_ -> new")
    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID, path);
    }

    @ProtocolHandler.Ticker
    public static void tick() {
        for (var playerEntry : players.entrySet()) {
            sendBoundingToPlayer(playerEntry.getKey(), playerEntry.getValue());
        }
    }

    @ProtocolHandler.ReloadServer
    public static void onServerReload() {
        if (LeavesConfig.protocol.bborProtocol) {
            initAllPlayer();
        } else {
            loggedOutAllPlayer();
        }
    }

    @ProtocolHandler.PlayerJoin
    public static void onPlayerLoggedIn(@NotNull ServerPlayer player) {
        ServerLevel overworld = MinecraftServer.getServer().overworld();
        ProtocolUtils.sendBytebufPacket(player, INITIALIZE_CLIENT, buf -> {
            buf.writeLong(overworld.getSeed());
            buf.writeInt(overworld.levelData.getSpawnPos().getX());
            buf.writeInt(overworld.levelData.getSpawnPos().getZ());
        });
        sendStructureList(player);
    }

    @ProtocolHandler.PlayerLeave
    public static void onPlayerLoggedOut(@NotNull ServerPlayer player) {
        players.remove(player.getId());
        playerBoundingBoxesCache.remove(player.getId());
    }

    @ProtocolHandler.BytebufReceiver(key = "subscribe")
    public static void onPlayerSubscribed(@NotNull ServerPlayer player, FriendlyByteBuf buf) {
        players.put(player.getId(), player);
        sendBoundingToPlayer(player.getId(), player);
    }

    @ProtocolHandler.ReloadDataPack
    public static void onDataPackReload() {
        players.values().forEach(BBORProtocol::sendStructureList);
    }

    public static void onChunkLoaded(@NotNull LevelChunk chunk) {
        Map<String, StructureStart> structures = new HashMap<>();
        final Registry<Structure> structureFeatureRegistry = chunk.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);
        for (var es : chunk.getAllStarts().entrySet()) {
            final var optional = structureFeatureRegistry.getResourceKey(es.getKey());
            optional.ifPresent(key -> structures.put(key.location().toString(), es.getValue()));
        }
        if (!structures.isEmpty()) {
            onStructuresLoaded(chunk.getLevel().dimension().location(), structures);
        }
    }

    public static void onStructuresLoaded(@NotNull ResourceLocation dimensionID, @NotNull Map<String, StructureStart> structures) {
        Map<BBoundingBox, Set<BBoundingBox>> cache = getOrCreateCache(dimensionID);
        for (var entry : structures.entrySet()) {
            StructureStart structureStart = entry.getValue();
            if (structureStart == null) {
                return;
            }

            String type = "structure:" + entry.getKey();
            BoundingBox bb = structureStart.getBoundingBox();
            BBoundingBox boundingBox = buildStructure(bb, type);
            if (cache.containsKey(boundingBox)) {
                return;
            }

            Set<BBoundingBox> structureBoundingBoxes = new HashSet<>();
            for (StructurePiece structureComponent : structureStart.getPieces()) {
                structureBoundingBoxes.add(buildStructure(structureComponent.getBoundingBox(), type));
            }
            cache.put(boundingBox, structureBoundingBoxes);
        }
    }

    private static @NotNull BBoundingBox buildStructure(@NotNull BoundingBox bb, String type) {
        BlockPos min = new BlockPos(bb.minX(), bb.minY(), bb.minZ());
        BlockPos max = new BlockPos(bb.maxX(), bb.maxY(), bb.maxZ());
        return new BBoundingBox(type, min, max);
    }

    private static void sendStructureList(@NotNull ServerPlayer player) {
        final Registry<Structure> structureRegistry = player.server.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        final Set<String> structureIds = structureRegistry.entrySet().stream()
            .map(e -> e.getKey().location().toString()).collect(Collectors.toSet());
        ProtocolUtils.sendBytebufPacket(player, STRUCTURE_LIST_SYNC, buf -> {
            buf.writeVarInt(structureIds.size());
            structureIds.forEach(buf::writeUtf);
        });
    }

    private static void sendBoundingToPlayer(int id, ServerPlayer player) {
        for (var entry : dimensionCache.entrySet()) {
            if (entry.getValue() == null) {
                return;
            }

            Set<BBoundingBox> playerBoundingBoxes = playerBoundingBoxesCache.computeIfAbsent(id, k -> new HashSet<>());
            Map<BBoundingBox, Set<BBoundingBox>> boundingBoxMap = entry.getValue();
            for (BBoundingBox key : boundingBoxMap.keySet()) {
                if (playerBoundingBoxes.contains(key)) {
                    continue;
                }

                Set<BBoundingBox> boundingBoxes = boundingBoxMap.get(key);
                ProtocolUtils.sendBytebufPacket(player, ADD_BOUNDING_BOX, buf -> {
                    buf.writeResourceLocation(entry.getKey());
                    key.serialize(buf);
                    if (boundingBoxes != null && boundingBoxes.size() > 1) {
                        for (BBoundingBox box : boundingBoxes) {
                            box.serialize(buf);
                        }
                    }
                });
                playerBoundingBoxes.add(key);
            }
        }
    }

    public static void initAllPlayer() {
        for (ServerPlayer player : MinecraftServer.getServer().getPlayerList().getPlayers()) {
            onPlayerLoggedIn(player);
        }
        initialized = true;
    }

    public static void loggedOutAllPlayer() {
        players.clear();
        playerBoundingBoxesCache.clear();
        for (var cache : dimensionCache.values()) {
            cache.clear();
        }
        dimensionCache.clear();
    }

    private static Map<BBoundingBox, Set<BBoundingBox>> getOrCreateCache(ResourceLocation dimensionId) {
        return dimensionCache.computeIfAbsent(dimensionId, dt -> new ConcurrentHashMap<>());
    }

    @Override
    public boolean isActive() {
        boolean active = LeavesConfig.protocol.bborProtocol;
        if (!active && initialized) {
            initialized = false;
            loggedOutAllPlayer();
        }
        return active;
    }

    private record BBoundingBox(String type, BlockPos min, BlockPos max) {

        private static int combineHashCodes(int @NotNull ... hashCodes) {
            final int prime = 31;
            int result = 0;
            for (int hashCode : hashCodes) {
                result = prime * result + hashCode;
            }
            return result;
        }

        public void serialize(@NotNull FriendlyByteBuf buf) {
            buf.writeChar('S');
            buf.writeInt(type.hashCode());
            buf.writeVarInt(min.getX()).writeVarInt(min.getY()).writeVarInt(min.getZ());
            buf.writeVarInt(max.getX()).writeVarInt(max.getY()).writeVarInt(max.getZ());
        }

        @Override
        public int hashCode() {
            return combineHashCodes(min.hashCode(), max.hashCode());
        }
    }
}
