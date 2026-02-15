package org.leavesmc.leaves.protocol.servux;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Bukkit;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.plugin.MinecraftInternalPlugin;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.leavesmc.leaves.util.TagUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Powered by Servux(https://github.com/sakura-ryoko/servux)

@LeavesProtocol.Register(namespace = "servux")
public class ServuxEntityDataProtocol implements LeavesProtocol {

    public static final int PROTOCOL_VERSION = 1;

    private static final Map<UUID, Long> readingSessionKeys = new HashMap<>();

    @ProtocolHandler.PlayerJoin
    public static void onPlayerJoin(ServerPlayer player) {
        sendMetadata(player);
    }

    @ProtocolHandler.PlayerLeave
    public static void onPlayerLeave(ServerPlayer player) {
        readingSessionKeys.remove(player.getUUID());
    }

    @ProtocolHandler.PayloadReceiver(payload = EntityDataPayload.class)
    public static void onPacketReceive(ServerPlayer player, EntityDataPayload payload) {
        switch (payload.packetType) {
            case PACKET_C2S_METADATA_REQUEST -> sendMetadata(player);
            case PACKET_C2S_BLOCK_ENTITY_REQUEST -> onBlockEntityRequest(player, payload.pos);
            case PACKET_C2S_ENTITY_REQUEST -> onEntityRequest(player, payload.entityId);
            case PACKET_C2S_NBT_RESPONSE_DATA -> {
                UUID uuid = player.getUUID();
                long readingSessionKey;

                if (!readingSessionKeys.containsKey(uuid)) {
                    readingSessionKey = RandomSource.create(Util.getMillis()).nextLong();
                    readingSessionKeys.put(uuid, readingSessionKey);
                } else {
                    readingSessionKey = readingSessionKeys.get(uuid);
                }

                FriendlyByteBuf fullPacket = PacketSplitter.receive(readingSessionKey, payload.buffer);

                if (fullPacket != null) {
                    readingSessionKeys.remove(uuid);
                    LeavesLogger.LOGGER.warning("ServuxEntityDataProtocol,PACKET_C2S_NBT_RESPONSE_DATA NOT Implemented!");
                }
            }
        }
    }

    public static void sendMetadata(ServerPlayer player) {
        CompoundTag metadata = new CompoundTag();
        metadata.putString("name", "entity_data");
        metadata.putString("id", EntityDataPayload.CHANNEL.toString());
        metadata.putInt("version", PROTOCOL_VERSION);
        metadata.putString("servux", ServuxProtocol.SERVUX_STRING);

        EntityDataPayload payload = new EntityDataPayload(EntityDataPayloadType.PACKET_S2C_METADATA);
        payload.nbt.merge(metadata);
        sendPacket(player, payload);
    }

    public static void onBlockEntityRequest(ServerPlayer player, BlockPos pos) {
        Bukkit.getGlobalRegionScheduler().run(MinecraftInternalPlugin.INSTANCE, (task) -> {
            BlockEntity be = player.level().getBlockEntity(pos);
            CompoundTag nbt = be != null ? be.saveWithFullMetadata(player.registryAccess()) : new CompoundTag();

            EntityDataPayload payload = new EntityDataPayload(EntityDataPayloadType.PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE);
            payload.pos = pos.immutable();
            payload.nbt.merge(nbt);
            sendPacket(player, payload);
        });
    }

    public static void onEntityRequest(ServerPlayer player, int entityId) {
        Bukkit.getGlobalRegionScheduler().run(MinecraftInternalPlugin.INSTANCE, (task) -> {
            Entity entity = player.level().getEntity(entityId);
            CompoundTag nbt = TagUtil.saveEntityWithoutId(entity);

            EntityDataPayload payload = new EntityDataPayload(EntityDataPayloadType.PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE);
            payload.entityId = entityId;
            payload.nbt.merge(nbt);
            sendPacket(player, payload);
        });
    }

    public static void sendPacket(ServerPlayer player, EntityDataPayload payload) {
        if (payload.packetType == EntityDataPayloadType.PACKET_S2C_NBT_RESPONSE_START) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeNbt(payload.nbt);
            PacketSplitter.send(ServuxEntityDataProtocol::sendWithSplitter, buffer, player);
        } else {
            ProtocolUtils.sendPayloadPacket(player, payload);
        }
    }

    private static void sendWithSplitter(ServerPlayer player, FriendlyByteBuf buf) {
        EntityDataPayload payload = new EntityDataPayload(EntityDataPayloadType.PACKET_S2C_NBT_RESPONSE_DATA);
        payload.buffer = buf;
        payload.nbt = new CompoundTag();
        sendPacket(player, payload);
    }

    @Override
    public boolean isActive() {
        return LeavesConfig.protocol.servux.entityProtocol;
    }

    public enum EntityDataPayloadType {
        PACKET_S2C_METADATA(1),
        PACKET_C2S_METADATA_REQUEST(2),
        PACKET_C2S_BLOCK_ENTITY_REQUEST(3),
        PACKET_C2S_ENTITY_REQUEST(4),
        PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE(5),
        PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE(6),
        // For Packet Splitter (Oversize Packets, S2C)
        PACKET_S2C_NBT_RESPONSE_START(10),
        PACKET_S2C_NBT_RESPONSE_DATA(11),
        // For Packet Splitter (Oversize Packets, C2S)
        PACKET_C2S_NBT_RESPONSE_START(12),
        PACKET_C2S_NBT_RESPONSE_DATA(13);

        public final int type;

        EntityDataPayloadType(int type) {
            this.type = type;
            EntityDataPayloadType.Helper.ID_TO_TYPE.put(type, this);
        }

        public static EntityDataPayloadType fromId(int id) {
            return EntityDataPayloadType.Helper.ID_TO_TYPE.get(id);
        }

        private static final class Helper {
            static Map<Integer, EntityDataPayloadType> ID_TO_TYPE = new HashMap<>();
        }
    }

    public static class EntityDataPayload implements LeavesCustomPayload {

        @ID
        public static final Identifier CHANNEL = ServuxProtocol.id("entity_data");

        @Codec
        public static final StreamCodec<FriendlyByteBuf, EntityDataPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.packetType.type);
                switch (payload.packetType) {
                    case PACKET_C2S_BLOCK_ENTITY_REQUEST -> {
                        buf.writeVarInt(payload.transactionId);
                        buf.writeBlockPos(payload.pos);
                    }
                    case PACKET_C2S_ENTITY_REQUEST -> {
                        buf.writeVarInt(payload.transactionId);
                        buf.writeVarInt(payload.entityId);
                    }
                    case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE -> {
                        buf.writeBlockPos(payload.pos);
                        buf.writeNbt(payload.nbt);
                    }
                    case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE -> {
                        buf.writeVarInt(payload.entityId);
                        buf.writeNbt(payload.nbt);
                    }
                    case PACKET_S2C_NBT_RESPONSE_DATA, PACKET_C2S_NBT_RESPONSE_DATA -> buf.writeBytes(payload.buffer.copy());
                    case PACKET_C2S_METADATA_REQUEST, PACKET_S2C_METADATA -> buf.writeNbt(payload.nbt);
                }
            },
            buf -> {
                EntityDataPayloadType type = EntityDataPayloadType.fromId(buf.readVarInt());
                if (type == null) {
                    throw new IllegalStateException("invalid packet type received");
                }
                EntityDataPayload payload = new EntityDataPayload(type);
                switch (type) {
                    case PACKET_C2S_BLOCK_ENTITY_REQUEST -> {
                        buf.readVarInt();
                        payload.pos = buf.readBlockPos().immutable();
                    }
                    case PACKET_C2S_ENTITY_REQUEST -> {
                        buf.readVarInt();
                        payload.entityId = buf.readVarInt();
                    }
                    case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE -> {
                        payload.pos = buf.readBlockPos().immutable();
                        CompoundTag nbt = buf.readNbt();
                        if (nbt != null) {
                            payload.nbt.merge(nbt);
                        }
                    }
                    case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE -> {
                        payload.entityId = buf.readVarInt();
                        CompoundTag nbt = buf.readNbt();
                        if (nbt != null) {
                            payload.nbt.merge(nbt);
                        }
                    }
                    case PACKET_S2C_NBT_RESPONSE_DATA, PACKET_C2S_NBT_RESPONSE_DATA -> {
                        payload.buffer = new FriendlyByteBuf(buf.readBytes(buf.readableBytes()));
                        payload.nbt = new CompoundTag();
                    }
                    case PACKET_C2S_METADATA_REQUEST, PACKET_S2C_METADATA -> {
                        CompoundTag nbt = buf.readNbt();
                        if (nbt != null) {
                            payload.nbt.merge(nbt);
                        }
                    }
                }
                return payload;
            }
        );

        private final EntityDataPayloadType packetType;
        private final int transactionId;
        private int entityId;
        private BlockPos pos;
        private CompoundTag nbt;
        private FriendlyByteBuf buffer;

        private EntityDataPayload(EntityDataPayloadType type) {
            this.packetType = type;
            this.transactionId = -1;
            this.entityId = -1;
            this.pos = BlockPos.ZERO;
            this.nbt = new CompoundTag();
            this.clearPacket();
        }

        private void clearPacket() {
            if (this.buffer != null) {
                this.buffer.clear();
                this.buffer = new FriendlyByteBuf(Unpooled.buffer());
            }
        }
    }
}