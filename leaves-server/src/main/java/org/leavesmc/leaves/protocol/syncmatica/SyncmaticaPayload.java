// LeavesMC - Protocol support for Syncmatica mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/piDemo/Syncmatica (LGPL-3.0 License)
// This file is licensed under the GNU General Public License v3.0.

package org.leavesmc.leaves.protocol.syncmatica;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;

public record SyncmaticaPayload(Identifier packetType, FriendlyByteBuf data) implements LeavesCustomPayload {

    @ID
    private static final Identifier NETWORK_ID = Identifier.tryBuild(SyncmaticaProtocol.PROTOCOL_ID, "main");

    @Codec
    private static final StreamCodec<FriendlyByteBuf, SyncmaticaPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeIdentifier(payload.packetType()).writeBytes(payload.data()),
        buf -> new SyncmaticaPayload(buf.readIdentifier(), new FriendlyByteBuf(buf.readBytes(buf.readableBytes())))
    );
}
