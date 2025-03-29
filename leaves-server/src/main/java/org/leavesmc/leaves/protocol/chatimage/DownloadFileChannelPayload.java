package org.leavesmc.leaves.protocol.chatimage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

public record DownloadFileChannelPayload(String message) implements LeavesCustomPayload<DownloadFileChannelPayload> {

    private static final ResourceLocation ID = ChatImageProtocol.id("download_file_channel");

    private static final StreamCodec<RegistryFriendlyByteBuf, DownloadFileChannelPayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.STRING_UTF8, DownloadFileChannelPayload::message, DownloadFileChannelPayload::new);

    @Override
    public void write(FriendlyByteBuf buf) {
        CODEC.encode(ProtocolUtils.decorate(buf), this);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @New
    public static DownloadFileChannelPayload create(ResourceLocation location, FriendlyByteBuf buf) {
        return CODEC.decode(ProtocolUtils.decorate(buf));
    }
}
