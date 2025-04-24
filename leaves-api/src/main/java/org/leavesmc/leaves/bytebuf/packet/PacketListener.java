package org.leavesmc.leaves.bytebuf.packet;

import org.bukkit.entity.Player;

@Deprecated
public interface PacketListener {

    Packet onPacketIn(Player player, Packet packet);

    Packet onPacketOut(Player player, Packet packet);
}
