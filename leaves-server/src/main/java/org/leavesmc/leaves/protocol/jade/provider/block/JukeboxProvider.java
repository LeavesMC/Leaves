package org.leavesmc.leaves.protocol.jade.provider.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;
import org.leavesmc.leaves.protocol.jade.accessor.BlockAccessor;
import org.leavesmc.leaves.protocol.jade.provider.StreamServerDataProvider;

public enum JukeboxProvider implements StreamServerDataProvider<BlockAccessor, ItemStack> {
    INSTANCE;

    private static final MapCodec<ItemStack> RECORD_CODEC = ItemStack.CODEC.fieldOf("record");
    private static final ResourceLocation MC_JUKEBOX = JadeProtocol.mc_id("jukebox");

    @Override
    public ItemStack streamData(BlockAccessor accessor) {
        return ((JukeboxBlockEntity) accessor.getBlockEntity()).getTheItem();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamCodec() {
        return ItemStack.OPTIONAL_STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return MC_JUKEBOX;
    }
}
