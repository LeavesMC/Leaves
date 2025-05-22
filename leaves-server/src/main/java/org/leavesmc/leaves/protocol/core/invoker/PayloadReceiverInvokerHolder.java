package org.leavesmc.leaves.protocol.core.invoker;

import net.minecraft.server.level.ServerPlayer;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;

import java.lang.reflect.Method;

public class PayloadReceiverInvokerHolder extends AbstractInvokerHolder<ProtocolHandler.PayloadReceiver> {
    public PayloadReceiverInvokerHolder(LeavesProtocol owner, Method invoker, ProtocolHandler.PayloadReceiver handler) {
        super(owner, invoker, handler, null, ServerPlayer.class, handler.payload());
    }

    public void invoke(ServerPlayer player, LeavesCustomPayload payload) {
        invoke0(false, player, payload);
    }
}
