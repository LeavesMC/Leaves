package org.leavesmc.leaves.protocol.rei.display;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.rei.ingredient.EntryIngredient;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;

public class CustomDisplay extends CraftingDisplay {
    private static final StreamCodec<RegistryFriendlyByteBuf, CustomDisplay> CODEC = StreamCodec.composite(
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        CustomDisplay::getInputEntries,
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        CustomDisplay::getOutputEntries,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
        CustomDisplay::getOptionalLocation,
        CustomDisplay::of
    );
    private static final ResourceLocation SERIALIZER_ID = ResourceLocation.tryBuild("minecraft", "default/crafting/custom");
    private final int width;
    private final int height;

    /**
     * see me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomDisplay#DefaultCustomDisplay
     */
    public CustomDisplay(@NotNull List<EntryIngredient> inputs, @NotNull List<EntryIngredient> outputs, @NotNull ResourceLocation location) {
        super(inputs, outputs, location);
        BitSet row = new BitSet(3);
        BitSet column = new BitSet(3);
        for (int i = 0; i < 9; i++)
            if (i < inputs.size()) {
                EntryIngredient stacks = inputs.get(i);
                if (stacks.stream().anyMatch(stack -> !stack.isEmpty())) {
                    row.set((i - (i % 3)) / 3);
                    column.set(i % 3);
                }
            }
        this.width = column.cardinality();
        this.height = row.cardinality();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static CustomDisplay of(@NotNull List<EntryIngredient> inputs, @NotNull List<EntryIngredient> outputs, @NotNull Optional<ResourceLocation> id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public ResourceLocation getSerializerId() {
        return SERIALIZER_ID;
    }

    public StreamCodec<RegistryFriendlyByteBuf, CustomDisplay> streamCodec() {
        return CODEC;
    }
}
