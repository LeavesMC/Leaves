package org.leavesmc.leaves.protocol.servux.litematics;

import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.leavesmc.leaves.protocol.servux.PacketSplitter;
import org.leavesmc.leaves.protocol.servux.ServuxProtocol;
import org.leavesmc.leaves.protocol.servux.litematics.placement.SchematicPlacement;
import org.leavesmc.leaves.protocol.servux.litematics.utils.NbtUtils;
import org.leavesmc.leaves.protocol.servux.litematics.utils.ReplaceBehavior;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@LeavesProtocol(namespace = "servux")
public class ServuxLitematicsProtocol {

    public static final ResourceLocation CHANNEL = ServuxProtocol.id("litematics");
    public static final int PROTOCOL_VERSION = 1;

    private static final CompoundTag metadata = new CompoundTag();
    private static final Map<UUID, Long> playerSession = new HashMap<>();

    @ProtocolHandler.Init
    public static void init() {
        metadata.putString("name", "litematic_data");
        metadata.putString("id", CHANNEL.toString());
        metadata.putInt("version", PROTOCOL_VERSION);
        metadata.putString("servux", ServuxProtocol.SERVUX_STRING);
    }

    public static boolean hasPermission(ServerPlayer player) {
        CraftPlayer bukkitEntity = player.getBukkitEntity();
        return bukkitEntity.hasPermission("leaves.protocol.litematics");
    }

    public static boolean isEnabled() {
        return LeavesConfig.protocol.servux.litematicsProtocol;
    }

    public static void encodeServerData(ServerPlayer player, ServuxLitematicaPayload packet) {
        if (packet.packetType.equals(ServuxLitematicaPayloadType.PACKET_S2C_NBT_RESPONSE_START)) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeVarInt(packet.getTransactionId());
            buffer.writeNbt(packet.getCompound());
            PacketSplitter.send(ServuxLitematicsProtocol::sendWithSplitter, buffer, player);
        } else {
            ProtocolUtils.sendPayloadPacket(player, packet);
        }
    }

    private static void sendWithSplitter(ServerPlayer player, FriendlyByteBuf buf) {
        ServuxLitematicaPayload payload = new ServuxLitematicaPayload(ServuxLitematicaPayloadType.PACKET_S2C_NBT_RESPONSE_DATA);
        payload.buffer = buf;
        payload.nbt = new CompoundTag();
        encodeServerData(player, payload);
    }

    @ProtocolHandler.PayloadReceiver(payload = ServuxLitematicaPayload.class, payloadId = "litematics")
    public static void onPacketReceive(ServerPlayer player, ServuxLitematicaPayload payload) {
        if (!isEnabled() || !hasPermission(player)) {
            return;
        }

        switch (payload.packetType) {
            case PACKET_C2S_METADATA_REQUEST -> {
                ServuxLitematicaPayload send = new ServuxLitematicaPayload(ServuxLitematicaPayloadType.PACKET_S2C_METADATA);
                send.nbt.merge(metadata);
                encodeServerData(player, send);
            }

            case PACKET_C2S_BULK_ENTITY_NBT_REQUEST -> onBulkEntityRequest(player, payload.getChunkPos(), payload.getCompound());

            case PACKET_C2S_NBT_RESPONSE_DATA -> {
                ServuxProtocol.LOGGER.debug("nbt response data");
                UUID uuid = player.getUUID();
                Long session = playerSession.getOrDefault(uuid, new Random().nextLong());
                playerSession.put(uuid, session);
                FriendlyByteBuf fullPacket = PacketSplitter.receive(session, payload.getBuffer());
                if (fullPacket == null) {
                    ServuxProtocol.LOGGER.debug("packet is none");
                    return;
                }
                playerSession.remove(uuid);
                CompoundTag compoundTag = fullPacket.readNbt();
                if (compoundTag == null) {
                    ServuxProtocol.LOGGER.error("cannot read nbt tag from packet");
                    return;
                }
                fullPacket.readVarInt();
                handleClientPasteRequest(player, compoundTag);
            }
        }
    }

    public static void onBulkEntityRequest(ServerPlayer player, ChunkPos chunkPos, CompoundTag req) {
        if (req == null || req.isEmpty()) {
            return;
        }

        ServerLevel world = player.serverLevel();
        ChunkAccess chunk = world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);

        if (chunk == null) {
            return;
        }
        if ((req.contains("Task") && req.getString("Task").equals("BulkEntityRequest")) ||
            !req.contains("Task")) {
            ServuxProtocol.LOGGER.debug("litematic_data: Sending Bulk NBT Data for ChunkPos [{}] to player {}", chunkPos, player.getName().getString());

            long timeStart = System.currentTimeMillis();
            ListTag tileList = new ListTag();
            ListTag entityList = new ListTag();
            int minY = req.getInt("minY");
            int maxY = req.getInt("maxY");
            BlockPos pos1 = new BlockPos(chunkPos.getMinBlockX(), minY, chunkPos.getMinBlockZ());
            BlockPos pos2 = new BlockPos(chunkPos.getMaxBlockX(), maxY, chunkPos.getMaxBlockZ());
            AABB bb = AABB.encapsulatingFullBlocks(pos1, pos2);
            Set<BlockPos> teSet = chunk.getBlockEntitiesPos();
            List<Entity> entities = world.getEntitiesOfClass(Entity.class, bb, e -> !(e instanceof Player));
            for (BlockPos tePos : teSet) {
                if ((tePos.getX() < chunkPos.getMinBlockX() || tePos.getX() > chunkPos.getMaxBlockX()) ||
                    (tePos.getZ() < chunkPos.getMinBlockZ() || tePos.getZ() > chunkPos.getMaxBlockZ()) ||
                    (tePos.getY() < minY || tePos.getY() > maxY)) {
                    continue;
                }

                BlockEntity be = world.getBlockEntity(tePos);
                CompoundTag beTag = be != null ? be.saveWithId(player.registryAccess()) : new CompoundTag();
                tileList.add(beTag);
            }

            for (Entity entity : entities) {
                CompoundTag entTag = new CompoundTag();

                if (entity.save(entTag)) {
                    Vec3 posVec = new Vec3(entity.getX() - pos1.getX(), entity.getY() - pos1.getY(), entity.getZ() - pos1.getZ());
                    NbtUtils.writeEntityPositionToTag(posVec, entTag);
                    entTag.putInt("entityId", entity.getId());
                    entityList.add(entTag);
                }
            }

            CompoundTag output = new CompoundTag();
            output.putString("Task", "BulkEntityReply");
            output.put("TileEntities", tileList);
            output.put("Entities", entityList);
            output.putInt("chunkX", chunkPos.x);
            output.putInt("chunkZ", chunkPos.z);
            ServuxProtocol.LOGGER.debug("process bulk entity used: {}ms", System.currentTimeMillis() - timeStart);

            ServuxLitematicaPayload send = new ServuxLitematicaPayload(ServuxLitematicaPayloadType.PACKET_S2C_NBT_RESPONSE_START);
            send.nbt.merge(output);
            encodeServerData(player, send);
        }
    }

    public static void handleClientPasteRequest(ServerPlayer player, CompoundTag tags) {
        if (tags.getString("Task").equals("LitematicaPaste")) {
            ServuxProtocol.LOGGER.debug("litematic_data: Servux Paste request from player {}", player.getName().getString());
            ServerLevel serverLevel = player.serverLevel();
            long timeStart = System.currentTimeMillis();
            SchematicPlacement placement = SchematicPlacement.createFromNbt(tags);
            ReplaceBehavior replaceMode = ReplaceBehavior.fromStringStatic(tags.getString("ReplaceMode"));
            MinecraftServer server = MinecraftServer.getServer();
            server.scheduleOnMain(() -> {
                placement.pasteTo(serverLevel, replaceMode);
                long timeElapsed = System.currentTimeMillis() - timeStart;
                player.getBukkitEntity().sendActionBar(Component.text("Pasted §b" + placement.getName() + "§r to world §d" + serverLevel.serverLevelData.getLevelName() + "§r in §a " + timeElapsed + "§rms."));
            });
        }
    }

    public enum ServuxLitematicaPayloadType {
        PACKET_S2C_METADATA(1),
        PACKET_C2S_METADATA_REQUEST(2),
        PACKET_C2S_BLOCK_ENTITY_REQUEST(3),
        PACKET_C2S_ENTITY_REQUEST(4),
        PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE(5),
        PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE(6),
        PACKET_C2S_BULK_ENTITY_NBT_REQUEST(7),
        // For Packet Splitter (Oversize Packets, S2C)
        PACKET_S2C_NBT_RESPONSE_START(10),
        PACKET_S2C_NBT_RESPONSE_DATA(11),
        // For Packet Splitter (Oversize Packets, C2S)
        PACKET_C2S_NBT_RESPONSE_START(12),
        PACKET_C2S_NBT_RESPONSE_DATA(13);

        private static final class Helper {
            static Map<Integer, ServuxLitematicaPayloadType> ID_TO_TYPE = new HashMap<>();
        }

        public final int type;

        ServuxLitematicaPayloadType(int type) {
            this.type = type;
            ServuxLitematicaPayloadType.Helper.ID_TO_TYPE.put(type, this);
        }

        public static ServuxLitematicaPayloadType fromId(int id) {
            return ServuxLitematicaPayloadType.Helper.ID_TO_TYPE.get(id);
        }
    }

    public static class ServuxLitematicaPayload implements LeavesCustomPayload<ServuxLitematicaPayload> {

        private final ServuxLitematicaPayloadType packetType;
        private final int transactionId;
        private int entityId;
        private BlockPos pos;
        private CompoundTag nbt;
        private ChunkPos chunkPos;
        private FriendlyByteBuf buffer;
        public static final int PROTOCOL_VERSION = 1;

        private ServuxLitematicaPayload(ServuxLitematicaPayloadType type) {
            this.packetType = type;
            this.transactionId = -1;
            this.entityId = -1;
            this.pos = BlockPos.ZERO;
            this.chunkPos = ChunkPos.ZERO;
            this.nbt = new CompoundTag();
            this.clearPacket();
        }

        private void clearPacket() {
            if (this.buffer != null) {
                this.buffer.clear();
                this.buffer = new FriendlyByteBuf(Unpooled.buffer());
            }
        }

        public int getVersion() {
            return PROTOCOL_VERSION;
        }

        public int getTransactionId() {
            return this.transactionId;
        }

        public int getEntityId() {
            return this.entityId;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public CompoundTag getCompound() {
            return this.nbt;
        }

        public ChunkPos getChunkPos() {
            return this.chunkPos;
        }

        public FriendlyByteBuf getBuffer() {
            return this.buffer;
        }

        public boolean hasBuffer() {
            return this.buffer != null && this.buffer.isReadable();
        }

        public boolean hasNbt() {
            return this.nbt != null && !this.nbt.isEmpty();
        }

        public boolean isEmpty() {
            return !this.hasBuffer() && !this.hasNbt();
        }

        @New
        public static ServuxLitematicaPayload decode(ResourceLocation location, FriendlyByteBuf input) {
            ServuxLitematicaPayloadType type = ServuxLitematicaPayloadType.fromId(input.readVarInt());
            if (type == null) {
                throw new IllegalStateException("invalid packet type received");
            }

            ServuxLitematicaPayload payload = new ServuxLitematicaPayload(type);
            switch (type) {
                case PACKET_C2S_BLOCK_ENTITY_REQUEST -> {
                    input.readVarInt();
                    payload.pos = input.readBlockPos().immutable();
                }

                case PACKET_C2S_ENTITY_REQUEST -> {
                    input.readVarInt();
                    payload.entityId = input.readVarInt();
                }

                case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE -> {
                    payload.pos = input.readBlockPos().immutable();
                    payload.nbt = input.readNbt();
                }

                case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE -> {
                    payload.entityId = input.readVarInt();
                    payload.nbt = input.readNbt();
                }

                case PACKET_C2S_BULK_ENTITY_NBT_REQUEST -> {
                    payload.chunkPos = input.readChunkPos();
                    payload.nbt = input.readNbt();
                }

                case PACKET_C2S_NBT_RESPONSE_DATA, PACKET_S2C_NBT_RESPONSE_DATA -> payload.buffer = new FriendlyByteBuf(input.readBytes(input.readableBytes()));

                case PACKET_C2S_METADATA_REQUEST, PACKET_S2C_METADATA -> payload.nbt = input.readNbt();
            }

            return payload;
        }

        @Override
        public void write(FriendlyByteBuf output) {
            output.writeVarInt(this.packetType.type);

            switch (this.packetType) {
                case PACKET_C2S_BLOCK_ENTITY_REQUEST -> {
                    output.writeVarInt(this.transactionId);
                    output.writeBlockPos(this.pos);
                }

                case PACKET_C2S_ENTITY_REQUEST -> {
                    output.writeVarInt(this.transactionId);
                    output.writeVarInt(this.entityId);
                }

                case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE -> {
                    output.writeBlockPos(this.pos);
                    output.writeNbt(this.nbt);
                }

                case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE -> {
                    output.writeVarInt(this.entityId);
                    output.writeNbt(this.nbt);
                }

                case PACKET_C2S_BULK_ENTITY_NBT_REQUEST -> {
                    output.writeChunkPos(this.chunkPos);
                    output.writeNbt(this.nbt);
                }

                case PACKET_S2C_NBT_RESPONSE_DATA, PACKET_C2S_NBT_RESPONSE_DATA -> output.writeBytes(this.buffer.readBytes(this.buffer.readableBytes()));

                case PACKET_C2S_METADATA_REQUEST, PACKET_S2C_METADATA -> output.writeNbt(this.nbt);

                default -> ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#toPacket: Unknown packet type!");
            }
        }

        @Override
        public ResourceLocation id() {
            return CHANNEL;
        }
    }
}