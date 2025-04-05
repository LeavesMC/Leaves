package org.leavesmc.leaves.protocol.chatimage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

public record FileInfoChannelPayload(String message) implements LeavesCustomPayload<FileInfoChannelPayload> {

    private static final ResourceLocation ID = ChatImageProtocol.id("file_info");

    private static final StreamCodec<RegistryFriendlyByteBuf, FileInfoChannelPayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.STRING_UTF8, FileInfoChannelPayload::message, FileInfoChannelPayload::new);

    @Override
    public void write(FriendlyByteBuf buf) {
        CODEC.encode(ProtocolUtils.decorate(buf), this);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @New
    public static FileInfoChannelPayload create(ResourceLocation location, FriendlyByteBuf buf) {
        return CODEC.decode(ProtocolUtils.decorate(buf));
    }
}