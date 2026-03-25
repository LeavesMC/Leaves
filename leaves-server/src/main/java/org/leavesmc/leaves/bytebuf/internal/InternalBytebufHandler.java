package org.leavesmc.leaves.bytebuf.internal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.kyori.adventure.key.Key;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.BundleDelimiterPacket;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bytebuf.Bytebuf;
import org.leavesmc.leaves.bytebuf.PacketAudience;
import org.leavesmc.leaves.bytebuf.PacketFlow;
import org.leavesmc.leaves.bytebuf.PacketType;
import org.leavesmc.leaves.bytebuf.SimpleBytebufAllocator;
import org.leavesmc.leaves.bytebuf.WrappedBytebuf;
import org.leavesmc.leaves.event.bytebuf.PacketEvent;
import org.leavesmc.leaves.event.bytebuf.PacketInEvent;
import org.leavesmc.leaves.event.bytebuf.PacketOutEvent;

import java.util.UUID;

import static org.leavesmc.leaves.bytebuf.internal.UniversalCodec.TYPE_MAP;

public class InternalBytebufHandler {

    private static final SimpleBytebufAllocator ALLOCATOR = new SimpleBytebufAllocator();
    private static final UniversalCodec CODEC = new UniversalCodec();

    public static void init() {
        ChannelInitializeListenerHolder.addListener(Key.key("leaves:bytebuf"), channel ->
            channel.pipeline().addBefore("packet_handler", PacketHandler.handlerName, new PacketHandler(channel)));
    }

    public static void updatePlayer(ServerPlayer player) {
        PacketHandler handler = (PacketHandler) player.connection.connection.channel.pipeline().get(PacketHandler.handlerName);
        handler.audienceHolder.setPlayer(player.getBukkitEntity());
    }

    public static SimpleBytebufAllocator allocator() {
        return ALLOCATOR;
    }

    public static net.minecraft.network.protocol.Packet<?> callPacketEvent(PacketAudience audience, net.minecraft.network.protocol.Packet<?> nmsPacket, PacketFlow bound) {
        if ((bound == PacketFlow.CLIENTBOUND && PacketOutEvent.getHandlerList().getRegisteredListeners().length == 0) ||
            (bound == PacketFlow.SERVERBOUND && PacketInEvent.getHandlerList().getRegisteredListeners().length == 0)) {
            return nmsPacket;
        }
        PacketType type = TYPE_MAP.get(nmsPacket.type());
        if (type == null) {
            return nmsPacket;
        }
        boolean isConfigStage = audience.getPlayer() == null;
        WrappedBytebuf bytebuf = new WrappedBytebuf(CODEC.encode(nmsPacket, isConfigStage));
        bytebuf.takeSnapshot();
        PacketEvent event = bound == PacketFlow.CLIENTBOUND ? new PacketOutEvent(audience, type, bytebuf) : new PacketInEvent(audience, type, bytebuf);
        if (!event.callEvent()) {
            return null;
        }
        WrappedBytebuf newBuf = (WrappedBytebuf) event.getBytebuf();
        RegistryFriendlyByteBuf registryBuf = newBuf.getRegistryBuf();
        // Maybe read in the listener? Reset reader index for decode.
        if (newBuf == bytebuf) {
            registryBuf.readerIndex(0);
        }
        return newBuf != bytebuf || bytebuf.isDirty() ? CODEC.decode(type, registryBuf, isConfigStage) : nmsPacket;
    }

    public static void sendPacket(PacketAudience audience, PacketType type, Bytebuf bytebuf) {
        Channel channel = (Channel) audience.getChannel();
        Connection connection = (Connection) channel.pipeline().get("packet_handler");
        connection.send(CODEC.decode(type, ((WrappedBytebuf) bytebuf).getRegistryBuf(), audience.getPlayer() == null));
    }

    private static class PacketHandler extends ChannelDuplexHandler {

        private final static String handlerName = "leaves-bytebuf-handler";

        private final AudienceHolder audienceHolder;

        public PacketHandler(Channel channel) {
            this.audienceHolder = new AudienceHolder(channel);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (!LeavesConfig.mics.leavesPacketEvent || msg instanceof BundlePacket<?> || msg instanceof BundleDelimiterPacket<?>) {
                super.channelRead(ctx, msg);
                return;
            }
            if (msg instanceof ServerboundHelloPacket(String name, UUID profileId)) {
                audienceHolder.setName(name);
            }
            if (msg instanceof net.minecraft.network.protocol.Packet<?> nmsPacket) {
                try {
                    msg = callPacketEvent(audienceHolder.get(), nmsPacket, PacketFlow.SERVERBOUND);
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
            if (!LeavesConfig.mics.leavesPacketEvent || msg instanceof BundlePacket<?> || msg instanceof BundleDelimiterPacket<?>) {
                super.write(ctx, msg, promise);
                return;
            }

            if (msg instanceof net.minecraft.network.protocol.Packet<?> nmsPacket) {
                try {
                    msg = callPacketEvent(audienceHolder.get(), nmsPacket, PacketFlow.CLIENTBOUND);
                } catch (Throwable t) {
                    MinecraftServer.LOGGER.error("Error on PacketInEvent.", t);
                }
            }

            if (msg != null) {
                super.write(ctx, msg, promise);
            }
        }
    }
}