package org.leavesmc.leaves.protocol.core;

import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public record InvokerHolder<T>(LeavesProtocol owner, Method invoker, T handler) {

    private void invoke0(Object... args) {
        if (!owner.isActive()) {
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
        invoke0();
    }

    public void invokePlayer(ServerPlayer player) {
        invoke0(player);
    }

    public void invokeBuf(ServerPlayer player, ByteBuf buf) {
        invoke0(player, ProtocolUtils.decorate(buf));
    }

    public void invokePayload(ServerPlayer player, LeavesCustomPayload<?> payload) {
        invoke0(player, payload);
    }

    public void invokeString(ServerPlayer player, String channelId) {
        invoke0(player, channelId);
    }
}
