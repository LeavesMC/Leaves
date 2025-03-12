package org.leavesmc.leaves.bytebuf;

import org.bukkit.plugin.Plugin;
import org.leavesmc.leaves.bytebuf.packet.PacketListener;

public interface BytebufManager {

    void registerListener(Plugin plugin, PacketListener listener);

    void unregisterListener(Plugin plugin, PacketListener listener);

    Bytebuf newBytebuf(int size);

    Bytebuf toBytebuf(byte[] bytes);
}
