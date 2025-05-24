package org.leavesmc.leaves.protocol.rei.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.rei.REIServerProtocol;

// TODO - ??? remove ???
public record BufCustomPacketPayload(
    Type<BufCustomPacketPayload> type,
    byte[] payload
) implements LeavesCustomPayload {

    @Codec
    public static final StreamCodec<FriendlyByteBuf, BufCustomPacketPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeByteArray(payload.payload),
        (buf) -> new BufCustomPacketPayload(null, buf.readByteArray())
    );

    @ID
    public static final ResourceLocation ID = REIServerProtocol.SYNC_DISPLAYS_PACKET;

    @NotNull
    @Override
    public Type<BufCustomPacketPayload> type() {
        return type;
    }
}
