package org.leavesmc.leaves.protocol.servux;

import io.netty.buffer.Unpooled;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Powered by Servux(https://github.com/sakura-ryoko/servux)

@LeavesProtocol(namespace = "servux")
public class ServuxEntityDataProtocol {

    public static final ResourceLocation CHANNEL = ServuxProtocol.id("entity_data");
    public static final int PROTOCOL_VERSION = 1;

    private static final Map<UUID, Long> readingSessionKeys = new HashMap<>();

    @ProtocolHandler.PlayerJoin
    public static void onPlayerLoggedIn(ServerPlayer player) {
        if (!LeavesConfig.protocol.servux.entityProtocol) {
            return;
        }

        sendMetadata(player);
    }

    @ProtocolHandler.PayloadReceiver(payload = EntityDataPayload.class, payloadId = "entity_data")
    public static void onPacketReceive(ServerPlayer player, EntityDataPayload payload) {
        if (!LeavesConfig.protocol.servux.entityProtocol) {
            return;
        }

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
        metadata.putString("id", CHANNEL.toString());
        metadata.putInt("version", PROTOCOL_VERSION);
        metadata.putString("servux", ServuxProtocol.SERVUX_STRING);

        EntityDataPayload payload = new EntityDataPayload(EntityDataPayloadType.PACKET_S2C_METADATA);
        payload.nbt.merge(metadata);
        sendPacket(player, payload);
    }

    public static void onBlockEntityRequest(ServerPlayer player, BlockPos pos) {
        MinecraftServer.getServer().execute(() -> {
            BlockEntity be = player.serverLevel().getBlockEntity(pos);
            CompoundTag nbt = be != null ? be.saveWithoutMetadata(player.registryAccess()) : new CompoundTag();

            EntityDataPayload payload = new EntityDataPayload(EntityDataPayloadType.PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE);
            payload.pos = pos.immutable();
            payload.nbt.merge(nbt);
            sendPacket(player, payload);
        });
    }

    public static void onEntityRequest(ServerPlayer player, int entityId) {
        MinecraftServer.getServer().execute(() -> {
            Entity entity = player.serverLevel().getEntity(entityId);
            CompoundTag nbt = entity != null ? entity.saveWithoutId(new CompoundTag()) : new CompoundTag();

            EntityDataPayload payload = new EntityDataPayload(EntityDataPayloadType.PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE);
            payload.entityId = entityId;
            payload.nbt.merge(nbt);
            sendPacket(player, payload);
        });
    }

    public static void sendPacket(ServerPlayer player, EntityDataPayload payload) {
        if (!LeavesConfig.protocol.servux.entityProtocol) {
            return;
        }

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

        private static final class Helper {
            static Map<Integer, EntityDataPayloadType> ID_TO_TYPE = new HashMap<>();
        }

        public final int type;

        EntityDataPayloadType(int type) {
            this.type = type;
            EntityDataPayloadType.Helper.ID_TO_TYPE.put(type, this);
        }

        public static EntityDataPayloadType fromId(int id) {
            return EntityDataPayloadType.Helper.ID_TO_TYPE.get(id);
        }
    }

    public static class EntityDataPayload implements LeavesCustomPayload<EntityDataPayload> {

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

        @New
        public static EntityDataPayload decode(ResourceLocation location, FriendlyByteBuf buffer) {
            EntityDataPayloadType type = EntityDataPayloadType.fromId(buffer.readVarInt());
            if (type == null) {
                throw new IllegalStateException("invalid packet type received");
            }

            EntityDataPayload payload = new EntityDataPayload(type);
            switch (type) {
                case PACKET_C2S_BLOCK_ENTITY_REQUEST -> {
                    buffer.readVarInt();
                    payload.pos = buffer.readBlockPos().immutable();
                }

                case PACKET_C2S_ENTITY_REQUEST -> {
                    buffer.readVarInt();
                    payload.entityId = buffer.readVarInt();
                }

                case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE -> {
                    payload.pos = buffer.readBlockPos().immutable();
                    CompoundTag nbt = buffer.readNbt();
                    if (nbt != null) {
                        payload.nbt.merge(nbt);
                    }
                }

                case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE -> {
                    payload.entityId = buffer.readVarInt();
                    CompoundTag nbt = buffer.readNbt();
                    if (nbt != null) {
                        payload.nbt.merge(nbt);
                    }
                }

                case PACKET_S2C_NBT_RESPONSE_DATA, PACKET_C2S_NBT_RESPONSE_DATA -> {
                    payload.buffer = new FriendlyByteBuf(buffer.readBytes(buffer.readableBytes()));
                    payload.nbt = new CompoundTag();
                }

                case PACKET_C2S_METADATA_REQUEST, PACKET_S2C_METADATA -> {
                    CompoundTag nbt = buffer.readNbt();
                    if (nbt != null) {
                        payload.nbt.merge(nbt);
                    }
                }
            }

            return payload;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(this.packetType.type);

            switch (this.packetType) {
                case PACKET_C2S_BLOCK_ENTITY_REQUEST -> {
                    buf.writeVarInt(this.transactionId);
                    buf.writeBlockPos(this.pos);
                }

                case PACKET_C2S_ENTITY_REQUEST -> {
                    buf.writeVarInt(this.transactionId);
                    buf.writeVarInt(this.entityId);
                }

                case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE -> {
                    buf.writeBlockPos(this.pos);
                    buf.writeNbt(this.nbt);
                }

                case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE -> {
                    buf.writeVarInt(this.entityId);
                    buf.writeNbt(this.nbt);
                }

                case PACKET_S2C_NBT_RESPONSE_DATA, PACKET_C2S_NBT_RESPONSE_DATA -> {
                    buf.writeBytes(this.buffer.readBytes(this.buffer.readableBytes()));
                }

                case PACKET_C2S_METADATA_REQUEST, PACKET_S2C_METADATA -> {
                    buf.writeNbt(this.nbt);
                }
            }
        }

        @Override
        public ResourceLocation id() {
            return CHANNEL;
        }
    }
}
