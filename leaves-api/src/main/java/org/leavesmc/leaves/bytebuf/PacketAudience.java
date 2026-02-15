package org.leavesmc.leaves.bytebuf;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public interface PacketAudience {

    void send(PacketType packetType, Bytebuf bytebuf);

    @Nullable Player getPlayer();

    @Nullable String getName();

    @NotNull Object getChannel();
}
