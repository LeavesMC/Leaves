package org.leavesmc.leaves.protocol.rei.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;

public class CreateItemHotbarPayload implements LeavesCustomPayload<CreateItemHotbarPayload> {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("roughlyenoughitems", "create_item_hotbar");

    private final ItemStack item;
    private final int hotbarSlot;

    public CreateItemHotbarPayload(final ItemStack item, final int hotbarSlot) {
        this.item = item;
        this.hotbarSlot = hotbarSlot;
    }

    @New
    public CreateItemHotbarPayload(FriendlyByteBuf buf) {
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

    public ItemStack getItem() {
        return item;
    }

    public int getHotbarSlot() {
        return hotbarSlot;
    }
}
