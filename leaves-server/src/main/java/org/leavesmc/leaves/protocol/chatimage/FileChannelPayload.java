// LeavesMC - Protocol support for ChatImage mod
// Copyright (c) 2024 LeavesMC
// Original source: https://github.com/kitUIN/ChatImage (GPL-3.0 License)
// This file is licensed under the GNU General Public License v3.0.

package org.leavesmc.leaves.protocol.chatimage;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;

public record FileChannelPayload(String message) implements LeavesCustomPayload {

    @ID
    private static final Identifier ID = ChatImageProtocol.id("get_file_channel");

    @Codec
    private static final StreamCodec<RegistryFriendlyByteBuf, FileChannelPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, FileChannelPayload::message, FileChannelPayload::new
    );
}