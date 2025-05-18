package org.leavesmc.leaves.protocol.core;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public interface LeavesCustomPayload<T extends LeavesCustomPayload<T>> extends CustomPacketPayload {

    @Override
    default @NotNull Type<? extends CustomPacketPayload> type() {
        throw new UnsupportedOperationException("Not supported");
    }
}
