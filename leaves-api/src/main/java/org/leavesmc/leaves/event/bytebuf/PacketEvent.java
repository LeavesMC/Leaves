package org.leavesmc.leaves.event.bytebuf;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.leavesmc.leaves.bytebuf.Bytebuf;
import org.leavesmc.leaves.bytebuf.PacketAudience;
import org.leavesmc.leaves.bytebuf.PacketType;

public abstract class PacketEvent extends Event implements Cancellable {

    private final PacketAudience audience;
    private final PacketType type;
    private final Bytebuf bytebuf;

    private boolean isCancelled = false;

    public PacketEvent(PacketAudience audience, PacketType type, Bytebuf bytebuf) {
        this.audience = audience;
        this.type = type;
        this.bytebuf = bytebuf;
    }

    public PacketType type() {
        return type;
    }

    public Bytebuf bytebuf() {
        return bytebuf;
    }

    public PacketAudience audience() {
        return audience;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }
}
