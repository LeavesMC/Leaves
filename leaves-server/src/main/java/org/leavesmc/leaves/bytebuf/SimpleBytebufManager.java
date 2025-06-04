package org.leavesmc.leaves.bytebuf;

import io.netty.buffer.Unpooled;
import org.bukkit.plugin.Plugin;
import org.leavesmc.leaves.bytebuf.internal.InternalBytebufHandler;
import org.leavesmc.leaves.bytebuf.packet.PacketListener;

public class SimpleBytebufManager implements BytebufManager {

    private final InternalBytebufHandler internal;

    public SimpleBytebufManager(InternalBytebufHandler internal) {
        this.internal = internal;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void registerListener(Plugin plugin, PacketListener listener) {
        internal.listenerMap.put(listener, plugin);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void unregisterListener(Plugin plugin, PacketListener listener) {
        internal.listenerMap.remove(listener);
    }

    @Override
    public Bytebuf newBytebuf(int size) {
        return new WrappedBytebuf(Unpooled.buffer(size));
    }

    @Override
    public Bytebuf toBytebuf(byte[] bytes) {
        return new WrappedBytebuf(Unpooled.wrappedBuffer(bytes));
    }
}
