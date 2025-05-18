package org.leavesmc.leaves.protocol.core;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface LeavesCustomPayload<B extends FriendlyByteBuf> extends CustomPacketPayload {

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ID {
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Codec {
    }

    interface Game extends LeavesCustomPayload<RegistryFriendlyByteBuf> {
    }

    interface Config extends LeavesCustomPayload<FriendlyByteBuf> {
    }

    @Override
    default @NotNull Type<? extends CustomPacketPayload> type() {
        throw new UnsupportedOperationException("Not supported");
    }
}
