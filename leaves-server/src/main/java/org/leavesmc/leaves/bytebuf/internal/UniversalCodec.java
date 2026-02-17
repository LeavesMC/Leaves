package org.leavesmc.leaves.bytebuf.internal;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.configuration.ConfigurationPacketTypes;
import net.minecraft.network.protocol.cookie.CookiePacketTypes;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.handshake.HandshakePacketTypes;
import net.minecraft.network.protocol.login.LoginPacketTypes;
import net.minecraft.network.protocol.ping.PingPacketTypes;
import net.minecraft.network.protocol.status.StatusPacketTypes;
import net.minecraft.server.MinecraftServer;
import org.leavesmc.leaves.bytebuf.PacketType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.function.Supplier;

@SuppressWarnings({"rawtypes", "unchecked"})
public class UniversalCodec {

    static final BiMap<net.minecraft.network.protocol.PacketType<?>, PacketType> TYPE_MAP;
    static final Supplier<RegistryFriendlyByteBuf> BUF_FACTORY = () -> new RegistryFriendlyByteBuf(Unpooled.buffer(), MinecraftServer.getServer().registryAccess());
    private static final BiMap<PacketType, StreamCodec> CODEC_MAP;
    private static final BiMap<String, PacketType> ID_MAP;

    static {
        ImmutableBiMap.Builder<String, PacketType> idBuilder = ImmutableBiMap.builder();
        ImmutableBiMap.Builder<net.minecraft.network.protocol.PacketType<?>, PacketType> typeBuilder = ImmutableBiMap.builder();
        ImmutableBiMap.Builder<PacketType, StreamCodec> codecBuilder = ImmutableBiMap.builder();
        Class<?>[] classes = new Class<?>[]{
            CommonPacketTypes.class, ConfigurationPacketTypes.class,
            CookiePacketTypes.class, GamePacketTypes.class, HandshakePacketTypes.class,
            LoginPacketTypes.class, PingPacketTypes.class, StatusPacketTypes.class
        };
        for (PacketType clientbound : PacketType.Clientbound.values()) {
            idBuilder.put(clientbound.id(), clientbound);
        }
        for (PacketType serverbound : PacketType.Serverbound.values()) {
            idBuilder.put(serverbound.id(), serverbound);
        }
        ID_MAP = idBuilder.buildOrThrow();
        for (Class<?> clazz : classes) {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    net.minecraft.network.protocol.PacketType<?> packetType = (net.minecraft.network.protocol.PacketType<?>) field.get(null);
                    if (!(field.getGenericType() instanceof ParameterizedType type)) {
                        continue;
                    }
                    Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>) type.getActualTypeArguments()[0];
                    Field codecField = packetClass.getDeclaredField("STREAM_CODEC");
                    codecField.setAccessible(true);
                    StreamCodec codec = (StreamCodec) codecField.get(null);
                    String id = packetType.flow().id() + "/" + packetType.id().getPath();
                    PacketType apiType = ID_MAP.get(id);
                    if (apiType == null) {
                        continue;
                    }
                    typeBuilder.put(packetType, apiType);
                    codecBuilder.put(apiType, codec);
                } catch (Throwable ignored) {
                }
            }
        }
        // Special case, there is no STREAM_CODEC in ClientboundCustomPayloadPacket.
        // CustomPayload's encode/decode should work with protocol stage context (Play/Config).
        typeBuilder.put(CommonPacketTypes.CLIENTBOUND_CUSTOM_PAYLOAD, PacketType.Clientbound.CUSTOM_PAYLOAD);
        TYPE_MAP = typeBuilder.build();
        CODEC_MAP = codecBuilder.build();
    }

    UniversalCodec() {
    }

    public RegistryFriendlyByteBuf encode(Packet<?> packet, boolean isConfigStage) {
        RegistryFriendlyByteBuf buf = BUF_FACTORY.get();
        if (packet instanceof ClientboundCustomPayloadPacket payloadPacket) {
            StreamCodec codec = isConfigStage ? ClientboundCustomPayloadPacket.CONFIG_STREAM_CODEC : ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC;
            codec.encode(buf, payloadPacket);
            return buf;
        }
        CODEC_MAP.get(TYPE_MAP.get(packet.type())).encode(buf, packet);
        return buf;
    }

    public Packet<?> decode(PacketType type, RegistryFriendlyByteBuf buf, boolean isConfigStage) {
        if (type == PacketType.Clientbound.CUSTOM_PAYLOAD) {
            StreamCodec codec = isConfigStage ? ClientboundCustomPayloadPacket.CONFIG_STREAM_CODEC : ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC;
            return (Packet<?>) codec.decode(buf);
        }
        return (Packet<?>) CODEC_MAP.get(type).decode(buf);
    }
}