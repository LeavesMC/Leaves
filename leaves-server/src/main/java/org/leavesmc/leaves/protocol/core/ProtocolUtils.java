package org.leavesmc.leaves.protocol.core;

import io.netty.buffer.ByteBuf;
import io.papermc.paper.ServerBuildInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class ProtocolUtils {

    private static final Function<ByteBuf, RegistryFriendlyByteBuf> bufDecorator = RegistryFriendlyByteBuf.decorator(MinecraftServer.getServer().registryAccess());

    public static String buildProtocolVersion(String protocol) {
        return protocol + "-leaves-" + ServerBuildInfo.buildInfo().asString(ServerBuildInfo.StringRepresentation.VERSION_SIMPLE);
    }

    public static void sendEmptyPayloadPacket(ServerPlayer player, ResourceLocation id) {
        player.connection.send(new ClientboundCustomPayloadPacket(new LeavesProtocolManager.EmptyPayload(id)));
    }

    @SuppressWarnings("all")
    public static void sendPayloadPacket(@NotNull ServerPlayer player, ResourceLocation id, Consumer<FriendlyByteBuf> consumer) {
        player.connection.send(new ClientboundCustomPayloadPacket(new DirectPayload(id, consumer)));
    }

    public static void sendPayloadPacket(ServerPlayer player, CustomPacketPayload payload) {
        player.connection.send(new ClientboundCustomPayloadPacket(payload));
    }

    public static RegistryFriendlyByteBuf decorate(ByteBuf buf) {
        return bufDecorator.apply(buf);
    }

    private record DirectPayload(ResourceLocation id, Consumer<FriendlyByteBuf> consumer) implements LeavesCustomPayload<DirectPayload> {
        @ProtocolHandler.Codec
        public static StreamCodec<FriendlyByteBuf, DirectPayload> CODEC = StreamCodec.of(
            (buffer, value) -> value.consumer().accept(buffer),
            buffer -> {
                throw new UnsupportedOperationException();
            }
        );
    }
}
