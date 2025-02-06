package org.leavesmc.leaves.protocol.jade.payload;


import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public interface ServerPayloadContext {
    default void execute(Runnable runnable) {
        Objects.requireNonNull(player().getServer()).execute(runnable);
    }

    default void sendPacket(CustomPacketPayload payload) {
        player().connection.send(new ClientboundCustomPayloadPacket(payload));
    }

    ServerPlayer player();
}