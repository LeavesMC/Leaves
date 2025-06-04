package org.leavesmc.leaves.bytebuf.internal;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.BundleDelimiterPacket;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bytebuf.BytebufManager;
import org.leavesmc.leaves.bytebuf.SimpleBytebufManager;
import org.leavesmc.leaves.bytebuf.WrappedBytebuf;
import org.leavesmc.leaves.bytebuf.packet.Packet;
import org.leavesmc.leaves.bytebuf.packet.PacketListener;
import org.leavesmc.leaves.bytebuf.packet.PacketType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.leavesmc.leaves.bytebuf.packet.PacketType.*;

@SuppressWarnings({"deprecation", "rawtypes", "unchecked"})
public class InternalBytebufHandler {

    private class PacketHandler extends ChannelDuplexHandler {

        private final static String handlerName = "leaves-bytebuf-handler";
        private final Player player;

        public PacketHandler(Player player) {
            this.player = player;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof BundlePacket<?> || msg instanceof BundleDelimiterPacket<?>) {
                super.channelRead(ctx, msg);
                return;
            }

            if (msg instanceof net.minecraft.network.protocol.Packet<?> nmsPacket) {
                try {
                    msg = callPacketInEvent(player, nmsPacket);
                } catch (Exception e) {
                    MinecraftServer.LOGGER.error("Error on PacketInEvent.", e);
                }
            }

            if (msg != null) {
                super.channelRead(ctx, msg);
            }
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof BundlePacket<?> || msg instanceof BundleDelimiterPacket<?>) {
                super.write(ctx, msg, promise);
                return;
            }

            if (msg instanceof net.minecraft.network.protocol.Packet<?> nmsPacket) {
                try {
                    msg = callPacketOutEvent(player, nmsPacket);
                } catch (Exception e) {
                    MinecraftServer.LOGGER.error("Error on PacketInEvent.", e);
                }
            }

            if (msg != null) {
                super.write(ctx, msg, promise);
            }
        }
    }

    private static final List<String> PACKET_PACKAGES = List.of(
        "net.minecraft.network.protocol.common",
        "net.minecraft.network.protocol.configuration",
        "net.minecraft.network.protocol.cookie",
        "net.minecraft.network.protocol.game",
        "net.minecraft.network.protocol.handshake",
        "net.minecraft.network.protocol.login",
        "net.minecraft.network.protocol.ping",
        "net.minecraft.network.protocol.status"
    );

    public final Map<PacketListener, Plugin> listenerMap = new HashMap<>();
    private final BytebufManager manager = new SimpleBytebufManager(this);
    private final ImmutableMap<PacketType, StreamCodec> type2CodecMap;
    private final Cache<net.minecraft.network.protocol.PacketType<?>, PacketType> resultCache = CacheBuilder.newBuilder().build();

    public InternalBytebufHandler() {
        ImmutableMap.Builder<PacketType, StreamCodec> builder = ImmutableMap.builder();

        for (PacketType packet : PacketType.values()) {
            String className = packet.name() + "Packet";
            for (String basePackage : PACKET_PACKAGES) {
                try {
                    Class<?> packetClass = Class.forName(basePackage + "." + className);
                    Field field = packetClass.getDeclaredField("STREAM_CODEC");
                    field.setAccessible(true);
                    builder.put(packet, (StreamCodec) field.get(null));
                } catch (Exception ignored) {
                }
            }
        }

        builder.put(ClientboundMoveEntityPos, ClientboundMoveEntityPacket.Pos.STREAM_CODEC);
        builder.put(ClientboundMoveEntityPosRot, ClientboundMoveEntityPacket.PosRot.STREAM_CODEC);
        builder.put(ClientboundMoveEntityRot, ClientboundMoveEntityPacket.Rot.STREAM_CODEC);
        builder.put(ServerboundMovePlayerPos, ServerboundMovePlayerPacket.Pos.STREAM_CODEC);
        builder.put(ServerboundMovePlayerPosRot, ServerboundMovePlayerPacket.PosRot.STREAM_CODEC);
        builder.put(ServerboundMovePlayerRot, ServerboundMovePlayerPacket.Rot.STREAM_CODEC);
        builder.put(ServerboundMovePlayerStatusOnly, ServerboundMovePlayerPacket.StatusOnly.STREAM_CODEC);
        builder.put(ClientboundCustomPayload, ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC);

        type2CodecMap = builder.build();
    }

    public void injectPlayer(ServerPlayer player) {
        if (LeavesConfig.mics.leavesPacketEvent) {
            player.connection.connection.channel.pipeline().addBefore("packet_handler", PacketHandler.handlerName, new PacketHandler(player.getBukkitEntity()));
        }
    }

    public BytebufManager getManager() {
        return manager;
    }

    public net.minecraft.network.protocol.Packet<?> callPacketInEvent(Player player, net.minecraft.network.protocol.Packet<?> nmsPacket) {
        if (listenerMap.isEmpty()) {
            return nmsPacket;
        }
        PacketType type = toEnumType(nmsPacket.type());
        if (type == null) {
            return nmsPacket;
        }
        Packet packet = createBytebufPacket(type, nmsPacket);
        for (PacketListener listener : listenerMap.keySet()) {
            if (listenerMap.get(listener).isEnabled()) {
                packet = listener.onPacketIn(player, packet);
                packet.bytebuf().resetReaderIndex();
            } else {
                listenerMap.remove(listener);
            }
        }
        return createNMSPacket(packet);
    }

    public net.minecraft.network.protocol.Packet<?> callPacketOutEvent(Player player, net.minecraft.network.protocol.Packet<?> nmsPacket) {
        if (listenerMap.isEmpty()) {
            return nmsPacket;
        }
        PacketType type = toEnumType(nmsPacket.type());
        if (type == null) {
            return nmsPacket;
        }
        Packet packet = createBytebufPacket(type, nmsPacket);
        for (PacketListener listener : listenerMap.keySet()) {
            if (listenerMap.get(listener).isEnabled()) {
                packet = listener.onPacketOut(player, packet);
                packet.bytebuf().resetReaderIndex();
            } else {
                listenerMap.remove(listener);
            }
        }
        return createNMSPacket(packet);
    }

    public void applyPacketToPlayer(ServerPlayer player, Packet packet) {
        Connection sp = player.connection.connection;
        sp.send(createNMSPacket(packet));
    }

    public net.minecraft.network.protocol.Packet<?> createNMSPacket(Packet packet) {
        StreamCodec<FriendlyByteBuf, net.minecraft.network.protocol.Packet<?>> codec = type2CodecMap.get(packet.type());
        if (codec == null) {
            throw new UnsupportedOperationException("This feature is not completely finished yet, packet type " + packet.type() + " is not supported temporary.");
        }
        return codec.decode(((WrappedBytebuf) packet.bytebuf()).getRegistryBuf());
    }

    @Nullable
    private PacketType toEnumType(net.minecraft.network.protocol.PacketType<?> type) {
        try {
            return this.resultCache.get(type, () -> {
                StringBuilder builder = new StringBuilder();
                String bound = type.toString().split("/")[0];
                String name = type.toString().split(":")[1];
                builder.append(bound.substring(0, 1).toUpperCase()).append(bound.substring(1));
                boolean flag = true;
                for (int i = 0; i < name.length(); i++) {
                    if (flag) {
                        builder.append(name.substring(i, i + 1).toUpperCase());
                        flag = false;
                        continue;
                    }
                    if (name.charAt(i) == '_') {
                        flag = true;
                    } else {
                        builder.append(name.charAt(i));
                    }
                }
                try {
                    return PacketType.valueOf(builder.toString());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception ignore) {
            return null;
        }
    }

    public Packet createBytebufPacket(PacketType type, net.minecraft.network.protocol.Packet<?> nmsPacket) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), MinecraftServer.getServer().registryAccess());
        StreamCodec<FriendlyByteBuf, net.minecraft.network.protocol.Packet<?>> codec = type2CodecMap.get(type);
        if (codec == null) {
            throw new UnsupportedOperationException("This feature is not completely finished yet, packet type " + type + " is not supported temporary.");
        }
        codec.encode(buf, nmsPacket);
        return new Packet(type, new WrappedBytebuf(buf));
    }
}