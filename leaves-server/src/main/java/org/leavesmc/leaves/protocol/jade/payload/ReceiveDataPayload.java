package org.leavesmc.leaves.protocol.jade.payload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;

public record ReceiveDataPayload(CompoundTag tag) implements LeavesCustomPayload {

    @ID
    private static final ResourceLocation PACKET_RECEIVE_DATA = JadeProtocol.id("receive_data");

    @Codec
    private static final StreamCodec<FriendlyByteBuf, ReceiveDataPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeNbt(payload.tag()),
        buf -> new ReceiveDataPayload(buf.readNbt())
    );
}
