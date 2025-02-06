package org.leavesmc.leaves.protocol.rei.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;

public class CreateItemPayload implements LeavesCustomPayload<CreateItemPayload> {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("roughlyenoughitems", "create_item");

    private final ItemStack item;

    @New
    public CreateItemPayload(ItemStack item) {
        this.item = item;
    }

    @New
    public CreateItemPayload(FriendlyByteBuf buf) {
        this(buf.readJsonWithCodec(ItemStack.OPTIONAL_CODEC));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, item);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public ItemStack getItem() {
        return item;
    }
}
