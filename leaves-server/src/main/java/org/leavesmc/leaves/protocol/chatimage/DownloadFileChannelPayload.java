package org.leavesmc.leaves.protocol.chatimage;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;

public record DownloadFileChannelPayload(String message) implements LeavesCustomPayload<DownloadFileChannelPayload> {

    @ProtocolHandler.ID
    private static final ResourceLocation ID = ChatImageProtocol.id("download_file_channel");

    @ProtocolHandler.Codec
    private static final StreamCodec<RegistryFriendlyByteBuf, DownloadFileChannelPayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.STRING_UTF8, DownloadFileChannelPayload::message, DownloadFileChannelPayload::new);
}
