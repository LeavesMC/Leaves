package org.leavesmc.leaves.protocol.jade.payload;


import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;

import java.util.List;
import java.util.Map;

import static org.leavesmc.leaves.protocol.jade.JadeProtocol.PRIMITIVE_STREAM_CODEC;

public record ServerHandshakePayload(
        Map<ResourceLocation, Object> serverConfig,
        List<Block> shearableBlocks,
        List<ResourceLocation> blockProviderIds,
        List<ResourceLocation> entityProviderIds) implements LeavesCustomPayload<ServerHandshakePayload> {

    private static final ResourceLocation PACKET_SERVER_HANDSHAKE = JadeProtocol.id("server_handshake");
    private static final StreamCodec<RegistryFriendlyByteBuf, ServerHandshakePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ResourceLocation.STREAM_CODEC, PRIMITIVE_STREAM_CODEC),
            ServerHandshakePayload::serverConfig,
            ByteBufCodecs.registry(Registries.BLOCK).apply(ByteBufCodecs.list()),
            ServerHandshakePayload::shearableBlocks,
            ByteBufCodecs.<ByteBuf, ResourceLocation>list().apply(ResourceLocation.STREAM_CODEC),
            ServerHandshakePayload::blockProviderIds,
            ByteBufCodecs.<ByteBuf, ResourceLocation>list().apply(ResourceLocation.STREAM_CODEC),
            ServerHandshakePayload::entityProviderIds,
            ServerHandshakePayload::new);

    @Override
    public void write(FriendlyByteBuf buf) {
        CODEC.encode(new RegistryFriendlyByteBuf(buf, MinecraftServer.getServer().registryAccess()), this);
    }

    @Override
    public ResourceLocation id() {
        return PACKET_SERVER_HANDSHAKE;
    }
}

