package org.leavesmc.leaves.bytebuf.internal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.BundleDelimiterPacket;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bytebuf.Bytebuf;
import org.leavesmc.leaves.bytebuf.PacketAudience;
import org.leavesmc.leaves.bytebuf.SimpleBytebufAllocator;
import org.leavesmc.leaves.bytebuf.WrappedBytebuf;
import org.leavesmc.leaves.bytebuf.PacketType;
import org.leavesmc.leaves.event.bytebuf.PacketInEvent;
import org.leavesmc.leaves.event.bytebuf.PacketOutEvent;

import static org.leavesmc.leaves.bytebuf.internal.UniversalCodec.ID_MAP;
import static org.leavesmc.leaves.bytebuf.internal.UniversalCodec.TYPE_MAP;

@SuppressWarnings({"rawtypes", "unchecked"})
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

    private static final SimpleBytebufAllocator ALLOCATOR = new SimpleBytebufAllocator();
    private static final UniversalCodec CODEC = new UniversalCodec();

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
        PacketType type = TYPE_MAP.get(nmsPacket.type());
        if (type == null) {
            return nmsPacket;
        }
        WrappedBytebuf bytebuf = new WrappedBytebuf(CODEC.encode(nmsPacket));
        bytebuf.takeSnapshot();
        if (!new PacketInEvent(audience, type, bytebuf).callEvent()) {
            return null;
        }
        return bytebuf.checkDirty() ? CODEC.decode(type, bytebuf.getRegistryBuf()) : nmsPacket;
    }

    public static net.minecraft.network.protocol.Packet<?> callPacketOutEvent(PacketAudience audience, net.minecraft.network.protocol.Packet<?> nmsPacket) {
        if (PacketOutEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return nmsPacket;
        }
        PacketType type = TYPE_MAP.get(nmsPacket.type());
        if (type == null) {
            return nmsPacket;
        }
        WrappedBytebuf bytebuf = new WrappedBytebuf(CODEC.encode(nmsPacket));
        bytebuf.takeSnapshot();
        if (!new PacketOutEvent(audience, type, bytebuf).callEvent()) {
            return null;
        }
        return bytebuf.checkDirty() ? CODEC.decode(type, bytebuf.getRegistryBuf()) : nmsPacket;
    }

    public static void sendPacket(PacketAudience audience, PacketType type, Bytebuf bytebuf) {
        Channel channel = (Channel) audience.getChannel();
        channel.write(CODEC.decode(type, ((WrappedBytebuf) bytebuf).getRegistryBuf()));
    }
}