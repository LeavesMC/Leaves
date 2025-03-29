package org.leavesmc.leaves.protocol.chatimage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

public record FileChannelPayload(String message) implements LeavesCustomPayload<FileChannelPayload> {

    private static final ResourceLocation ID = ChatImageProtocol.id("get_file_channel");

    private static final StreamCodec<RegistryFriendlyByteBuf, FileChannelPayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.STRING_UTF8, FileChannelPayload::message, FileChannelPayload::new);

    @Override
    public void write(FriendlyByteBuf buf) {
        CODEC.encode(ProtocolUtils.decorate(buf), this);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @New
    public static FileChannelPayload create(ResourceLocation location, FriendlyByteBuf buf) {
        return CODEC.decode(ProtocolUtils.decorate(buf));
    }
}