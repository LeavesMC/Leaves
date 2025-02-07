package org.leavesmc.leaves.protocol.rei.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.rei.REIServerProtocol;

public record CreateItemPayload(ItemStack item) implements LeavesCustomPayload<CreateItemPayload> {

    private static final ResourceLocation ID = REIServerProtocol.id("create_item");

    @New
    public CreateItemPayload(ResourceLocation location, @NotNull FriendlyByteBuf buf) {
        this(buf.readJsonWithCodec(ItemStack.OPTIONAL_CODEC));
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, item);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
