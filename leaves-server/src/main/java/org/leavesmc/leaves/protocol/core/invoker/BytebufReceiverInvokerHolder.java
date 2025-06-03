package org.leavesmc.leaves.protocol.core.invoker;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;

import java.lang.reflect.Method;

public class BytebufReceiverInvokerHolder extends AbstractInvokerHolder<ProtocolHandler.BytebufReceiver> {
    public BytebufReceiverInvokerHolder(LeavesProtocol owner, Method invoker, ProtocolHandler.BytebufReceiver handler) {
        super(owner, invoker, handler, null, ServerPlayer.class, FriendlyByteBuf.class);
    }

    public boolean invoke(ServerPlayer player, FriendlyByteBuf buf) {
        return invoke0(false, player, buf) instanceof Boolean b && b;
    }
}
