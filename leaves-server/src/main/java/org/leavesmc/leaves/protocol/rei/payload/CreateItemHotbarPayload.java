package org.leavesmc.leaves.protocol.rei.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.rei.REIServerProtocol;

public record CreateItemHotbarPayload(ItemStack item, int hotbarSlot) implements LeavesCustomPayload<CreateItemHotbarPayload> {

    private static final ResourceLocation ID = REIServerProtocol.id("create_item_hotbar");

    @New
    public CreateItemHotbarPayload(ResourceLocation location, FriendlyByteBuf buf) {
        this(buf.readJsonWithCodec(ItemStack.OPTIONAL_CODEC), buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, item);
        buf.writeVarInt(hotbarSlot);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
