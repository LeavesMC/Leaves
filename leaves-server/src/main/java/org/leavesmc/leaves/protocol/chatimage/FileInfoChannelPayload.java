package org.leavesmc.leaves.protocol.chatimage;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;

public record FileInfoChannelPayload(String message) implements LeavesCustomPayload<FileInfoChannelPayload> {

    @ProtocolHandler.ID
    private static final ResourceLocation ID = ChatImageProtocol.id("file_info");

    @ProtocolHandler.Codec
    private static final StreamCodec<RegistryFriendlyByteBuf, FileInfoChannelPayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.STRING_UTF8, FileInfoChannelPayload::message, FileInfoChannelPayload::new);
}