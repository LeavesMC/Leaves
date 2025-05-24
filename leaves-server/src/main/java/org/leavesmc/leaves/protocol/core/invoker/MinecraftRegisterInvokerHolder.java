package org.leavesmc.leaves.protocol.core.invoker;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;

import java.lang.reflect.Method;

public class MinecraftRegisterInvokerHolder extends AbstractInvokerHolder<ProtocolHandler.MinecraftRegister> {
    public MinecraftRegisterInvokerHolder(LeavesProtocol owner, Method invoker, ProtocolHandler.MinecraftRegister handler) {
        super(owner, invoker, handler, null, ServerPlayer.class, ResourceLocation.class);
    }

    public void invoke(ServerPlayer player, ResourceLocation id) {
        invoke0(false, player, id);
    }
}
