package org.leavesmc.leaves.protocol.servux.litematics;

import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@LeavesProtocol(namespace = "servux")
public class ServuxLitematicsProtocol {

    private static final CompoundTag metadata = new CompoundTag();
    private static final Map<UUID, Long> playerSession = new HashMap<>();
    private static final Map<String, Map<String, String>> translations = new HashMap<>(
        Map.of(
            "en_us", Map.of(
                "servux.litematics.success.pasted", "servux.litematics.success.pasted"
            ),
            "zh_cn", Map.of(
                "servux.litematics.success.pasted", "已将 §b%s§r 粘贴至世界 §d%s§r，耗时 §a%s§r 毫秒"
            )
        )
    );

    @ProtocolHandler.Init
    public static void init() {
        Server server = Bukkit.getServer();
        PluginManager pluginManager = server.getPluginManager();
        if (pluginManager.getPermission("leaves.protocol.litematics") == null) {
            pluginManager.addPermission(new Permission("leaves.protocol.litematics", PermissionDefault.OP));
        }
        metadata.putString("name", "litematic_data");
        metadata.putString("id", "servux:litematics");
        metadata.putInt("version", 1);
        metadata.putString("servux", ServuxProtocol.SERVUX_STRING);
    }

    @Nullable
    public static String getTranslation(String key) {
        Map<String, String> translation = translations.computeIfAbsent(LeavesConfig.mics.serverLang, ignored -> translations.get("en_us"));
        return translation.get(key);
    }

    public static Component getTranslationComponent(String key, Object... args) {
        String translation = getTranslation(key);
        List<TextComponent> components = Stream.of(args).map(Object::toString).map(Component::text).toList();
        if (translation != null) {
            return Component.translatable(key, translation, components);
        }
        return Component.translatable(key, components);
    }

    public static boolean hasPermission(ServerPlayer player) {
        CraftPlayer bukkitEntity = player.getBukkitEntity();
        return bukkitEntity.hasPermission("leaves.protocol.litematics");
    }

    public static boolean isEnabled() {
        return LeavesConfig.protocol.servux.litematicsProtocol;
    }

    public static void encodeServerData(ServerPlayer player, ServuxLitematicaPacket packet) {
        if (packet.packetType.equals(ServuxLitematicaPacket.Type.PACKET_S2C_NBT_RESPONSE_START)) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeVarInt(packet.getTransactionId());
            buffer.writeNbt(packet.getCompound());
            PacketSplitter.send(ServuxLitematicsProtocol::sendWithSplitter, buffer, player);
            return;
        }
        ProtocolUtils.sendPayloadPacket(player, new ServuxLitematicaPacket.ServuxLitematicaPayload(packet));
    }

    private static void sendWithSplitter(ServerPlayer player, FriendlyByteBuf buf) {
        ProtocolUtils.sendPayloadPacket(player, new ServuxLitematicaPacket.ServuxLitematicaPayload(ServuxLitematicaPacket.ResponseS2CData(buf)));
    }

    @ProtocolHandler.PayloadReceiver(payload = ServuxLitematicaPacket.ServuxLitematicaPayload.class, payloadId = "litematics")
    public static void onPacketReceive(ServerPlayer player, ServuxLitematicaPacket.ServuxLitematicaPayload payload) {
        if (!isEnabled()) return;
        if (!hasPermission(player)) return;
        ServuxLitematicaPacket data = payload.data;
        switch (data.packetType) {
            case PACKET_C2S_METADATA_REQUEST -> ProtocolUtils.sendPayloadPacket(player, new ServuxLitematicaPacket.ServuxLitematicaPayload(ServuxLitematicaPacket.MetadataResponse(metadata)));
            case PACKET_C2S_BULK_ENTITY_NBT_REQUEST -> {
                ChunkPos pos = data.getChunkPos();
                CompoundTag compound = data.getCompound();
                onBulkEntityRequest(player, pos, compound);
            }
            case PACKET_C2S_NBT_RESPONSE_DATA -> {
                ServuxProtocol.LOGGER.debug("nbt response data");
                UUID uuid = player.getUUID();
                Long session = playerSession.getOrDefault(uuid, new Random().nextLong());
                playerSession.put(uuid, session);
                FriendlyByteBuf fullPacket = PacketSplitter.receive(session, data.getBuffer());
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
            long timeElapsed = System.currentTimeMillis() - timeStart;
            ServuxProtocol.LOGGER.debug("process bulk entity used: {}ms", timeElapsed);
            encodeServerData(player, ServuxLitematicaPacket.ResponseS2CStart(output));
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
                player.getBukkitEntity().sendActionBar(getTranslationComponent("servux.litematics.success.pasted", placement.getName(), serverLevel.registryAccess(), timeElapsed));
            });
        }
    }

    public static class ServuxLitematicaPacket {

        private final Type packetType;
        private final int transactionId;
        private int entityId;
        private BlockPos pos;
        private CompoundTag nbt;
        private ChunkPos chunkPos;
        private FriendlyByteBuf buffer;
        public static final int PROTOCOL_VERSION = 1;

        private ServuxLitematicaPacket(Type type) {
            this.packetType = type;
            this.transactionId = -1;
            this.entityId = -1;
            this.pos = BlockPos.ZERO;
            this.chunkPos = ChunkPos.ZERO;
            this.nbt = new CompoundTag();
            this.clearPacket();
        }

        public static ServuxLitematicaPacket MetadataRequest(@Nullable CompoundTag nbt) {
            ServuxLitematicaPacket packet = new ServuxLitematicaPacket(Type.PACKET_C2S_METADATA_REQUEST);
            if (nbt != null) {
                packet.nbt.merge(nbt);
            }
            return packet;
        }

        public static ServuxLitematicaPacket MetadataResponse(@Nullable CompoundTag nbt) {
            var packet = new ServuxLitematicaPacket(Type.PACKET_S2C_METADATA);
            if (nbt != null) {
                packet.nbt.merge(nbt);
            }
            return packet;
        }

        // Entity simple response
        public static ServuxLitematicaPacket SimpleEntityResponse(int entityId, @Nullable CompoundTag nbt) {
            var packet = new ServuxLitematicaPacket(Type.PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE);
            if (nbt != null) {
                packet.nbt.merge(nbt);
            }
            packet.entityId = entityId;
            return packet;
        }

        public static ServuxLitematicaPacket SimpleBlockResponse(BlockPos pos, @Nullable CompoundTag nbt) {
            var packet = new ServuxLitematicaPacket(Type.PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE);
            if (nbt != null) {
                packet.nbt.merge(nbt);
            }
            packet.pos = pos.immutable();
            return packet;
        }

        public static ServuxLitematicaPacket BlockEntityRequest(BlockPos pos) {
            var packet = new ServuxLitematicaPacket(Type.PACKET_C2S_BLOCK_ENTITY_REQUEST);
            packet.pos = pos.immutable();
            return packet;
        }

        public static ServuxLitematicaPacket EntityRequest(int entityId) {
            var packet = new ServuxLitematicaPacket(Type.PACKET_C2S_ENTITY_REQUEST);
            packet.entityId = entityId;
            return packet;
        }

        public static ServuxLitematicaPacket BulkNbtRequest(ChunkPos chunkPos, @Nullable CompoundTag nbt) {
            var packet = new ServuxLitematicaPacket(Type.PACKET_C2S_BULK_ENTITY_NBT_REQUEST);
            packet.chunkPos = chunkPos;
            if (nbt != null) {
                packet.nbt.merge(nbt);
            }
            return packet;
        }

        // Nbt Packet, using Packet Splitter
        public static ServuxLitematicaPacket ResponseS2CStart(@Nonnull CompoundTag nbt) {
            var packet = new ServuxLitematicaPacket(Type.PACKET_S2C_NBT_RESPONSE_START);
            packet.nbt.merge(nbt);
            return packet;
        }

        public static ServuxLitematicaPacket ResponseS2CData(@Nonnull FriendlyByteBuf buffer) {
            var packet = new ServuxLitematicaPacket(Type.PACKET_S2C_NBT_RESPONSE_DATA);
            packet.buffer = new FriendlyByteBuf(buffer.copy());
            packet.nbt = new CompoundTag();
            return packet;
        }

        public static ServuxLitematicaPacket ResponseC2SData(@Nonnull FriendlyByteBuf buffer) {
            var packet = new ServuxLitematicaPacket(Type.PACKET_C2S_NBT_RESPONSE_DATA);
            packet.buffer = new FriendlyByteBuf(buffer.copy());
            packet.nbt = new CompoundTag();
            return packet;
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

        public int getPacketType() {
            return this.packetType.get();
        }

        public Type getType() {
            return this.packetType;
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

        public void toPacket(FriendlyByteBuf output) {
            output.writeVarInt(this.packetType.get());

            switch (this.packetType) {
                case PACKET_C2S_BLOCK_ENTITY_REQUEST -> {
                    // Write BE Request
                    try {
                        output.writeVarInt(this.transactionId);
                        output.writeBlockPos(this.pos);
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#toPacket: error writing Block Entity Request to packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_C2S_ENTITY_REQUEST -> {
                    // Write Entity Request
                    try {
                        output.writeVarInt(this.transactionId);
                        output.writeVarInt(this.entityId);
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#toPacket: error writing Entity Request to packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE -> {
                    try {
                        output.writeBlockPos(this.pos);
                        output.writeNbt(this.nbt);
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#toPacket: error writing Block Entity Response to packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE -> {
                    try {
                        output.writeVarInt(this.entityId);
                        output.writeNbt(this.nbt);
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#toPacket: error writing Entity Response to packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_C2S_BULK_ENTITY_NBT_REQUEST -> {
                    try {
                        output.writeChunkPos(this.chunkPos);
                        output.writeNbt(this.nbt);
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#toPacket: error writing Bulk Entity Request to packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_S2C_NBT_RESPONSE_DATA, PACKET_C2S_NBT_RESPONSE_DATA -> {
                    // Write Packet Buffer (Slice)
                    try {
                        output.writeBytes(this.buffer.copy());
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#toPacket: error writing buffer data to packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_C2S_METADATA_REQUEST, PACKET_S2C_METADATA -> {
                    // Write NBT
                    try {
                        output.writeNbt(this.nbt);
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#toPacket: error writing NBT to packet: [{}]", e.getLocalizedMessage());
                    }
                }
                default -> ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#toPacket: Unknown packet type!");
            }
        }

        @Nullable
        public static ServuxLitematicaPacket fromPacket(FriendlyByteBuf input) {
            int i = input.readVarInt();
            Type type = getType(i);

            if (type == null) {
                // Invalid Type
                ServuxProtocol.LOGGER.warn("ServuxLitematicaPacket#fromPacket: invalid packet type received");
                return null;
            }
            switch (type) {
                case PACKET_C2S_BLOCK_ENTITY_REQUEST -> {
                    // Read Packet Buffer
                    try {
                        input.readVarInt(); // todo: old code compat
                        return ServuxLitematicaPacket.BlockEntityRequest(input.readBlockPos());
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#fromPacket: error reading Block Entity Request from packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_C2S_ENTITY_REQUEST -> {
                    // Read Packet Buffer
                    try {
                        input.readVarInt(); // todo: old code compat
                        return ServuxLitematicaPacket.EntityRequest(input.readVarInt());
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#fromPacket: error reading Entity Request from packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE -> {
                    try {
                        return ServuxLitematicaPacket.SimpleBlockResponse(input.readBlockPos(), input.readNbt());
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#fromPacket: error reading Block Entity Response from packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE -> {
                    try {
                        return ServuxLitematicaPacket.SimpleEntityResponse(input.readVarInt(), input.readNbt());
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#fromPacket: error reading Entity Response from packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_C2S_BULK_ENTITY_NBT_REQUEST -> {
                    try {
                        return ServuxLitematicaPacket.BulkNbtRequest(input.readChunkPos(), input.readNbt());
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#fromPacket: error reading Bulk Entity Request from packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_S2C_NBT_RESPONSE_DATA -> {
                    // Read Packet Buffer Slice
                    try {
                        return ServuxLitematicaPacket.ResponseS2CData(new FriendlyByteBuf(input.readBytes(input.readableBytes())));
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#fromPacket: error reading S2C Bulk Response Buffer from packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_C2S_NBT_RESPONSE_DATA -> {
                    // Read Packet Buffer Slice
                    try {
                        return ServuxLitematicaPacket.ResponseC2SData(new FriendlyByteBuf(input.readBytes(input.readableBytes())));
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#fromPacket: error reading C2S Bulk Response Buffer from packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_C2S_METADATA_REQUEST -> {
                    // Read Nbt
                    try {
                        return ServuxLitematicaPacket.MetadataRequest(input.readNbt());
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#fromPacket: error reading Metadata Request from packet: [{}]", e.getLocalizedMessage());
                    }
                }
                case PACKET_S2C_METADATA -> {
                    // Read Nbt
                    try {
                        return ServuxLitematicaPacket.MetadataResponse(input.readNbt());
                    } catch (Exception e) {
                        ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#fromPacket: error reading Metadata Response from packet: [{}]", e.getLocalizedMessage());
                    }
                }
                default -> ServuxProtocol.LOGGER.error("ServuxLitematicaPacket#fromPacket: Unknown packet type!");
            }

            return null;
        }

        @Nullable
        public static Type getType(int input) {

            for (Type type : Type.values()) {
                if (type.get() == input) {
                    return type;
                }
            }

            return null;
        }

        public enum Type {
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

            private final int type;

            Type(int type) {
                this.type = type;
            }

            int get() {
                return this.type;
            }
        }

        public record ServuxLitematicaPayload(ServuxLitematicaPacket data) implements LeavesCustomPayload<ServuxLitematicaPayload> {
            public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ServuxProtocol.PROTOCOL_ID, "litematics");

            public ServuxLitematicaPayload(FriendlyByteBuf input) {
                this(fromPacket(input));
            }

            @Nonnull
            @New
            public static ServuxLitematicaPayload read(ResourceLocation ignoredId, FriendlyByteBuf buf) {
                return new ServuxLitematicaPayload(buf);
            }

            public void write(FriendlyByteBuf output) {
                data.toPacket(output);
            }

            @Override
            public ResourceLocation id() {
                return ID;
            }
        }
    }
}