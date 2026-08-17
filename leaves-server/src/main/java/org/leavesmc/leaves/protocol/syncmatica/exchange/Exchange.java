// LeavesMC - Protocol support for Syncmatica mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/piDemo/Syncmatica (LGPL-3.0 License)
// This file is licensed under the GNU General Public License v3.0.

package org.leavesmc.leaves.protocol.syncmatica.exchange;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public interface Exchange {
    ExchangeTarget getPartner();

    boolean checkPacket(Identifier id, FriendlyByteBuf packetBuf);

    void handle(Identifier id, FriendlyByteBuf packetBuf);

    boolean isFinished();

    boolean isSuccessful();

    void close(boolean notifyPartner);

    void init();
}
