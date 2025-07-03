package org.leavesmc.leaves.bot;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerBotPacketListenerImpl extends ServerGamePacketListenerImpl {

    public ServerBotPacketListenerImpl(MinecraftServer server, ServerBot bot) {
        super(server, BotConnection.INSTANCE, bot, CommonListenerCookie.createInitial(bot.gameProfile, false));
    }

    @Override
    public void send(@NotNull Packet<?> packet, @Nullable ChannelFutureListener listener) {
    }

    @Override
    public void disconnect(@NotNull DisconnectionDetails disconnectionInfo) {
    }

    @Override
    public boolean isAcceptingMessages() {
        return true;
    }

    @Override
    public void tick() {
    }

    public static class BotConnection extends Connection {

        private static final BotConnection INSTANCE = new BotConnection();

        public BotConnection() {
            super(PacketFlow.SERVERBOUND);
        }

        @Override
        public void tick() {
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public boolean isConnecting() {
            return false;
        }

        @Override
        public boolean isMemoryConnection() {
            return false;
        }

        @Override
        public void send(@NotNull Packet<?> packet, @javax.annotation.Nullable ChannelFutureListener channelFutureListener, boolean flag) {
        }
    }
}
