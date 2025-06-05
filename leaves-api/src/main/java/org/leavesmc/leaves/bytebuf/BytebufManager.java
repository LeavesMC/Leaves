package org.leavesmc.leaves.bytebuf;

import org.bukkit.plugin.Plugin;
import org.leavesmc.leaves.bytebuf.packet.PacketListener;

public interface BytebufManager {

    @Deprecated
    void registerListener(Plugin plugin, PacketListener listener);

    @Deprecated
    void unregisterListener(Plugin plugin, PacketListener listener);

    Bytebuf newBytebuf(int size);

    Bytebuf toBytebuf(byte[] bytes);
}
