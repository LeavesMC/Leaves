package org.leavesmc.leaves.bytebuf.packet;

import org.leavesmc.leaves.bytebuf.Bytebuf;

public record Packet(PacketType type, Bytebuf bytebuf) {
}
