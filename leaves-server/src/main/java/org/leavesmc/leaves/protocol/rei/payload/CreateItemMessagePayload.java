package org.leavesmc.leaves.protocol.rei.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.rei.REIServerProtocol;

public record CreateItemMessagePayload(ItemStack item, String playerName) implements LeavesCustomPayload<CreateItemMessagePayload> {

    private static final ResourceLocation ID = REIServerProtocol.id("ci_msg");

    @New
    public CreateItemMessagePayload(ResourceLocation location, @NotNull FriendlyByteBuf buf) {
        this(buf.readJsonWithCodec(ItemStack.OPTIONAL_CODEC), buf.readUtf());
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, item);
        buf.writeUtf(playerName);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
