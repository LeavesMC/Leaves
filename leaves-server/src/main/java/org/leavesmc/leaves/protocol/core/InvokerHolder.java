package org.leavesmc.leaves.protocol.core;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public record InvokerHolder<T>(LeavesProtocol owner, Method invoker, T handler) {

    private void invoke0(boolean force, Object... args) {
        if (!force && !owner.isActive()) {
            return;
        }
        try {
            if (Modifier.isStatic(invoker.getModifiers())) {
                invoker.invoke(null, args);
            } else {
                invoker.invoke(owner, args);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void invokeEmpty() {
        invoke0(false);
    }

    public void invokeInit() {
        invoke0(true);
    }

    public void invokePlayer(ServerPlayer player) {
        invoke0(false, player);
    }

    public void invokeBuf(ServerPlayer player, ByteBuf buf) {
        invoke0(false, player, ProtocolUtils.decorate(buf));
    }

    public void invokePayload(ServerPlayer player, LeavesCustomPayload payload) {
        invoke0(false, player, payload);
    }

    public void invokeKey(ServerPlayer player, ResourceLocation key) {
        invoke0(false, player, key.toString());
    }
}
