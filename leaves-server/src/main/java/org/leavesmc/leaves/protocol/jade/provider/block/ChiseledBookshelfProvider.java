package org.leavesmc.leaves.protocol.jade.provider.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;
import org.leavesmc.leaves.protocol.jade.accessor.BlockAccessor;
import org.leavesmc.leaves.protocol.jade.provider.StreamServerDataProvider;

public enum ChiseledBookshelfProvider implements StreamServerDataProvider<BlockAccessor, ItemStack> {
    INSTANCE;

    public static final MapCodec<ItemStack> BOOK_CODEC = ItemStack.CODEC.fieldOf("book");
    private static final ResourceLocation MC_CHISELED_BOOKSHELF = JadeProtocol.mc_id("chiseled_bookshelf");

    @Override
    public ItemStack streamData(BlockAccessor accessor) {
        int slot = ((ChiseledBookShelfBlock) accessor.getBlock()).getHitSlot(accessor.getHitResult(), accessor.getBlockState()).orElse(-1);
        if (slot == -1) {
            return null;
        }
        return ((ChiseledBookShelfBlockEntity) accessor.getBlockEntity()).getItem(slot);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamCodec() {
        return ItemStack.OPTIONAL_STREAM_CODEC;
    }


    @Override
    public ResourceLocation getUid() {
        return MC_CHISELED_BOOKSHELF;
    }
}
