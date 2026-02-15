package org.leavesmc.leaves.event.bytebuf;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bytebuf.Bytebuf;
import org.leavesmc.leaves.bytebuf.PacketAudience;
import org.leavesmc.leaves.bytebuf.PacketType;

public class PacketOutEvent extends PacketEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PacketOutEvent(PacketAudience audience, PacketType packetType, Bytebuf bytebuf) {
        super(audience, packetType, bytebuf);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
