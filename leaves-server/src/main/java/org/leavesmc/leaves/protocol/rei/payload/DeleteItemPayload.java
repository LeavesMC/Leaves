package org.leavesmc.leaves.protocol.rei.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;

public class DeleteItemPayload implements LeavesCustomPayload<DeleteItemPayload> {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("roughlyenoughitems", "delete_item");

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
