package org.leavesmc.leaves.protocol.core;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface LeavesCustomPayload<T extends LeavesCustomPayload<T>> extends CustomPacketPayload {

    default ResourceLocation id() {
        return null;
    }

    @Override
    @NotNull
    default Type<T> type() {
        return new CustomPacketPayload.Type<>(id());
    }
}
