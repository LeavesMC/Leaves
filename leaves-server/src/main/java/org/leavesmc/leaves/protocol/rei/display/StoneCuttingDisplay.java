package org.leavesmc.leaves.protocol.rei.display;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.bukkit.craftbukkit.CraftRegistry;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.rei.ingredient.EntryIngredient;

import java.util.List;
import java.util.Optional;

/**
 * see me.shedaniel.rei.plugin.common.displays.DefaultStoneCuttingDisplay
 */
public class StoneCuttingDisplay extends Display {
    private static final StreamCodec<RegistryFriendlyByteBuf, StoneCuttingDisplay> CODEC = StreamCodec.composite(
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        StoneCuttingDisplay::getInputEntries,
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        StoneCuttingDisplay::getOutputEntries,
        ByteBufCodecs.optional(Identifier.STREAM_CODEC),
        StoneCuttingDisplay::getOptionalLocation,
        StoneCuttingDisplay::of
    );

    private static final Identifier SERIALIZER_ID = Identifier.tryBuild("minecraft", "default/stone_cutting");

    public StoneCuttingDisplay(@NotNull List<EntryIngredient> inputs, @NotNull List<EntryIngredient> outputs, @NotNull Identifier id) {
        super(inputs, outputs, id);
    }

    public StoneCuttingDisplay(RecipeHolder<StonecutterRecipe> recipeHolder) {
        this(
            List.of(EntryIngredient.ofIngredient(recipeHolder.value().input())),
            List.of(EntryIngredient.of(recipeHolder.value().assemble(new SingleRecipeInput(ItemStack.EMPTY), CraftRegistry.getMinecraftRegistry()))),
            recipeHolder.id().identifier()
        );
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static StoneCuttingDisplay of(@NotNull List<EntryIngredient> inputs, @NotNull List<EntryIngredient> outputs, @NotNull Optional<Identifier> id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER_ID;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, StoneCuttingDisplay> streamCodec() {
        return CODEC;
    }
}
