package org.leavesmc.leaves.protocol.rei.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;

public class CreateItemMessagePayload implements LeavesCustomPayload<CreateItemMessagePayload> {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("roughlyenoughitems", "ci_msg");

    private final ItemStack item;
    private final String playerName;

    public CreateItemMessagePayload(final ItemStack item, final String playerName) {
        this.item = item;
        this.playerName = playerName;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, item);
        buf.writeUtf(playerName);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getPlayerName() {
        return playerName;
    }
}
