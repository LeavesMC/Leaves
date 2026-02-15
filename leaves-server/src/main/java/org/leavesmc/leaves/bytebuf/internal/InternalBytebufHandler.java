package org.leavesmc.leaves.bytebuf.internal;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bytebuf.Bytebuf;
import org.leavesmc.leaves.bytebuf.PacketAudience;
import org.leavesmc.leaves.bytebuf.SimpleBytebufAllocator;
import org.leavesmc.leaves.bytebuf.WrappedBytebuf;
import org.leavesmc.leaves.bytebuf.PacketType;
import org.leavesmc.leaves.event.bytebuf.PacketInEvent;
import org.leavesmc.leaves.event.bytebuf.PacketOutEvent;

import java.lang.reflect.Field;
import java.util.List;

import static org.leavesmc.leaves.bytebuf.PacketType.*;

@SuppressWarnings({"deprecation", "rawtypes", "unchecked"})
public class InternalBytebufHandler {

    private static class PacketHandler extends ChannelDuplexHandler {

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
                } catch (Throwable t) {
                    MinecraftServer.LOGGER.error("Error on PacketInEvent.", t);
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
                } catch (Throwable t) {
                    MinecraftServer.LOGGER.error("Error on PacketInEvent.", t);
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

    private static final SimpleBytebufAllocator ALLOCATOR = new SimpleBytebufAllocator();
    private static final ImmutableMap<PacketType, StreamCodec> TYPE_CODEC_MAP;
    private static final Cache<net.minecraft.network.protocol.PacketType<?>, PacketType> resultCache = CacheBuilder.newBuilder().build();

    static {
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

        TYPE_CODEC_MAP = builder.build();
    }

    public static InternalBytebufHandler INSTANCE = new InternalBytebufHandler();

    private InternalBytebufHandler() {}

    public void injectPlayer(ServerPlayer player) {
        if (LeavesConfig.mics.leavesPacketEvent) {
            player.connection.connection.channel.pipeline().addBefore("packet_handler", PacketHandler.handlerName, new PacketHandler(player.getBukkitEntity()));
        }
    }

    public static SimpleBytebufAllocator allocator() {
        return ALLOCATOR;
    }

    public static net.minecraft.network.protocol.Packet<?> callPacketInEvent(PacketAudience audience, net.minecraft.network.protocol.Packet<?> nmsPacket) {
        if (PacketInEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return nmsPacket;
        }
        PacketType type = toEnumType(nmsPacket.type());
        if (type == null) {
            return nmsPacket;
        }
        WrappedBytebuf bytebuf = createWrappedBytebuf(type, nmsPacket);
        bytebuf.takeSnapshot();
        if (!new PacketInEvent(audience, type, bytebuf).callEvent()) {
            return null;
        }
        return bytebuf.checkDirty() ? createNMSPacket(type, bytebuf) : nmsPacket;
    }

    public static net.minecraft.network.protocol.Packet<?> callPacketOutEvent(PacketAudience audience, net.minecraft.network.protocol.Packet<?> nmsPacket) {
        if (PacketOutEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return nmsPacket;
        }
        PacketType type = toEnumType(nmsPacket.type());
        if (type == null) {
            return nmsPacket;
        }
        WrappedBytebuf bytebuf = createWrappedBytebuf(type, nmsPacket);
        bytebuf.takeSnapshot();
        if (!new PacketOutEvent(audience, type, bytebuf).callEvent()) {
            return null;
        }
        return bytebuf.checkDirty() ? createNMSPacket(type, bytebuf) : nmsPacket;
    }

    public static void sendPacket(PacketAudience audience, PacketType type, Bytebuf bytebuf) {
        Channel channel = (Channel) audience.getChannel();
        channel.write(createNMSPacket(type, bytebuf));
    }

    public static net.minecraft.network.protocol.Packet<?> createNMSPacket(PacketType type, Bytebuf bytebuf) {
        StreamCodec<FriendlyByteBuf, net.minecraft.network.protocol.Packet<?>> codec = TYPE_CODEC_MAP.get(type);
        if (codec == null) {
            throw new UnsupportedOperationException("This feature is not completely finished yet, packet type " + type + " is not supported temporary.");
        }
        return codec.decode((((WrappedBytebuf) bytebuf).getRegistryBuf()));
    }

    @Nullable
    private static PacketType toEnumType(net.minecraft.network.protocol.PacketType<?> type) {
        try {
            return resultCache.get(type, () -> {
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

    public static WrappedBytebuf createWrappedBytebuf(PacketType type, net.minecraft.network.protocol.Packet<?> nmsPacket) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), MinecraftServer.getServer().registryAccess());
        StreamCodec<FriendlyByteBuf, net.minecraft.network.protocol.Packet<?>> codec = TYPE_CODEC_MAP.get(type);
        if (codec == null) {
            throw new UnsupportedOperationException("This feature is not completely finished yet, packet type " + type + " is not supported temporary.");
        }
        codec.encode(buf, nmsPacket);
        return new WrappedBytebuf(buf);
    }
}