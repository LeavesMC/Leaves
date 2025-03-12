package org.leavesmc.leaves.bytebuf.packet;

import org.bukkit.entity.Player;

public interface PacketListener {

    Packet onPacketIn(Player player, Packet packet);

    Packet onPacketOut(Player player, Packet packet);
}
