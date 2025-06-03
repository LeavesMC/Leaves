package org.leavesmc.leaves.protocol.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.papermc.paper.ServerBuildInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class ProtocolUtils {

    private static final Function<ByteBuf, RegistryFriendlyByteBuf> bufDecorator = buf -> buf instanceof RegistryFriendlyByteBuf registry ? registry : new RegistryFriendlyByteBuf(buf, MinecraftServer.getServer().registryAccess());

    public static String buildProtocolVersion(String protocol) {
        return protocol + "-leaves-" + ServerBuildInfo.buildInfo().asString(ServerBuildInfo.StringRepresentation.VERSION_SIMPLE);
    }

    public static void sendEmptyPacket(ServerPlayer player, ResourceLocation id) {
        player.internalConnection.send(new ClientboundCustomPayloadPacket(new DiscardedPayload(id, null)));
    }

    public static void sendBytebufPacket(@NotNull ServerPlayer player, ResourceLocation id, Consumer<? super RegistryFriendlyByteBuf> consumer) {
        RegistryFriendlyByteBuf buf = decorate(Unpooled.buffer());
        consumer.accept(buf);
        player.internalConnection.send(new ClientboundCustomPayloadPacket(new DiscardedPayload(id, ByteBufUtil.getBytes(buf))));
    }

    public static void sendPayloadPacket(ServerPlayer player, CustomPacketPayload payload) {
        player.internalConnection.send(new ClientboundCustomPayloadPacket(payload));
    }

    public static RegistryFriendlyByteBuf decorate(ByteBuf buf) {
        return bufDecorator.apply(buf);
    }
}
