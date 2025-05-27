package org.leavesmc.leaves.protocol.rei;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.leavesmc.leaves.LeavesLogger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class PacketTransformer {

    private static final byte START = 0x0;
    private static final byte PART = 0x1;
    private static final byte END = 0x2;
    private static final byte ONLY = 0x3;

    private final Map<UUID, PartData> cache = Collections.synchronizedMap(new HashMap<>());

    public static DiscardedPayload wrapRei(ResourceLocation location, FriendlyByteBuf buf) {
        FriendlyByteBuf newBuf = new FriendlyByteBuf(Unpooled.buffer());
        newBuf.writeByteArray(ByteBufUtil.getBytes(buf));
        return new DiscardedPayload(location, ByteBufUtil.getBytes(newBuf));
    }

    public void inbound(ResourceLocation id, RegistryFriendlyByteBuf buf, ServerPlayer player, BiConsumer<ResourceLocation, RegistryFriendlyByteBuf> consumer) {
        UUID key = player.getUUID();
        PartData data;
        switch (buf.readByte()) {
            case START -> {
                int partsNum = buf.readInt();
                data = new PartData(id, partsNum);
                if (cache.put(key, data) != null) {
                    LeavesLogger.LOGGER.warning("Received invalid START packet for SplitPacketTransformer with packet id " + id);
                }
                buf.retain();
                data.parts.add(buf);
            }
            case PART -> {
                if ((data = cache.get(key)) == null) {
                    LeavesLogger.LOGGER.warning("Received invalid PART packet for SplitPacketTransformer with packet id " + id);
                    buf.release();
                } else if (!data.id.equals(id)) {
                    LeavesLogger.LOGGER.warning("Received invalid PART packet for SplitPacketTransformer with packet id " + id + ", id in cache is {}" + data.id);
                    buf.release();
                    for (RegistryFriendlyByteBuf part : data.parts) {
                        if (part != buf) {
                            part.release();
                        }
                    }
                    cache.remove(key);
                } else {
                    buf.retain();
                    data.parts.add(buf);
                }
            }
            case END -> {
                if ((data = cache.get(key)) == null) {
                    LeavesLogger.LOGGER.warning("Received invalid END packet for SplitPacketTransformer with packet id {}" + id);
                    buf.release();
                } else if (!data.id.equals(id)) {
                    LeavesLogger.LOGGER.warning("Received invalid END packet for SplitPacketTransformer with packet id " + id + ", id in cache is {}" + data.id);
                    buf.release();
                    for (RegistryFriendlyByteBuf part : data.parts) {
                        if (part != buf) {
                            part.release();
                        }
                    }
                    cache.remove(key);
                } else {
                    buf.retain();
                    data.parts.add(buf);
                }
                if (data == null) {
                    return;
                }
                if (data.parts.size() != data.partsNum) {
                    LeavesLogger.LOGGER.warning("Received invalid END packet for SplitPacketTransformer with packet id " + id + " with size " + data.parts + ", parts expected is {}" + data.partsNum);
                    for (RegistryFriendlyByteBuf part : data.parts) {
                        if (part != buf) {
                            part.release();
                        }
                    }
                } else {
                    RegistryFriendlyByteBuf byteBuf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data.parts.toArray(new ByteBuf[0])), buf.registryAccess());
                    consumer.accept(data.id, byteBuf);
                    byteBuf.release();
                }
                cache.remove(key);
            }
            case ONLY -> consumer.accept(id, buf);
            default -> throw new IllegalStateException("Illegal split packet header!");
        }
    }

    public void outbound(ResourceLocation id, RegistryFriendlyByteBuf buf, BiConsumer<ResourceLocation, RegistryFriendlyByteBuf> consumer) {
        int maxSize = 1048576 - 1 - 20 - id.toString().getBytes(StandardCharsets.UTF_8).length;
        if (buf.readableBytes() <= maxSize) {
            ByteBuf stateBuf = Unpooled.buffer(1);
            stateBuf.writeByte(ONLY);
            RegistryFriendlyByteBuf packetBuffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(stateBuf, buf), buf.registryAccess());
            consumer.accept(id, packetBuffer);
        } else {
            int partSize = maxSize - 4;
            int parts = (int) Math.ceil(buf.readableBytes() / (float) partSize);
            for (int i = 0; i < parts; i++) {
                RegistryFriendlyByteBuf packetBuffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), buf.registryAccess());
                if (i == 0) {
                    packetBuffer.writeByte(START);
                    packetBuffer.writeInt(parts);
                } else if (i == parts - 1) {
                    packetBuffer.writeByte(END);
                } else {
                    packetBuffer.writeByte(PART);
                }
                int next = Math.min(buf.readableBytes(), partSize);
                packetBuffer.writeBytes(buf.retainedSlice(buf.readerIndex(), next));
                buf.skipBytes(next);
                consumer.accept(id, packetBuffer);
            }
            buf.release();
        }
    }

    private static class PartData {
        private final ResourceLocation id;
        private final int partsNum;
        private final List<RegistryFriendlyByteBuf> parts;

        public PartData(ResourceLocation id, int partsNum) {
            this.id = id;
            this.partsNum = partsNum;
            this.parts = new ArrayList<>();
        }
    }
}
