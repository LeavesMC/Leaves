package org.leavesmc.leaves.protocol.rei.payload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.rei.REIServerProtocol;

public record MoveItemPayload(ResourceLocation category, boolean isShift, CompoundTag nbt) implements LeavesCustomPayload<MoveItemPayload> {

    private static final ResourceLocation ID = REIServerProtocol.id("move_items_new");

    @New
    public MoveItemPayload(ResourceLocation location, @NotNull FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readBoolean(), buf.readNbt());
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(category);
        buf.writeBoolean(isShift);
        buf.writeNbt(nbt);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
