// LeavesMC - Protocol support for Jade mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/snownee/Jade (MIT License)
// This file is licensed under the GNU General Public License v3.0.

package org.leavesmc.leaves.protocol.jade.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;

public record ClientHandshakePayload(String protocolVersion) implements LeavesCustomPayload {

    @ID
    private static final Identifier PACKET_CLIENT_HANDSHAKE = JadeProtocol.id("client_handshake");

    @Codec
    private static final StreamCodec<RegistryFriendlyByteBuf, ClientHandshakePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ClientHandshakePayload::protocolVersion, ClientHandshakePayload::new
    );
}