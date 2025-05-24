package org.leavesmc.leaves.protocol.rei.display;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.craftbukkit.CraftRegistry;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.rei.ingredient.EntryIngredient;

import java.util.List;
import java.util.Optional;

public class ShapelessDisplay extends CraftingDisplay {
    private static final StreamCodec<RegistryFriendlyByteBuf, CraftingDisplay> CODEC = StreamCodec.composite(
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        CraftingDisplay::getInputEntries,
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        CraftingDisplay::getOutputEntries,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
        CraftingDisplay::getOptionalLocation,
        ShapelessDisplay::of
    );

    private static final ResourceLocation SERIALIZER_ID = ResourceLocation.tryBuild("minecraft", "default/crafting/shapeless");

    public ShapelessDisplay(@NotNull List<EntryIngredient> inputs,
                            @NotNull List<EntryIngredient> outputs,
                            @NotNull ResourceLocation location) {
        super(inputs, outputs, location);
    }

    public ShapelessDisplay(@NotNull RecipeHolder<ShapelessRecipe> recipeHolder) {
        this(
            recipeHolder.value().placementInfo().ingredients().stream().map(EntryIngredient::ofIngredient).toList(),
            List.of(EntryIngredient.of(recipeHolder.value().assemble(CraftingInput.EMPTY, CraftRegistry.getMinecraftRegistry()))),
            recipeHolder.id().location()
        );
    }

    public ShapelessDisplay(@NotNull ShapelessCraftingRecipeDisplay recipeDisplay, ResourceLocation id) {
        this(
            ofSlotDisplays(recipeDisplay.ingredients()),
            List.of(ofSlotDisplay(recipeDisplay.result())),
            id
        );
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static CraftingDisplay of(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location) {
        throw new NotImplementedException();
    }

    @Override
    public int getWidth() {
        return getInputEntries().size() > 4 ? 3 : 2;
    }

    @Override
    public int getHeight() {
        return getInputEntries().size() > 4 ? 3 : 2;
    }

    @Override
    public ResourceLocation getSerializerId() {
        return SERIALIZER_ID;
    }

    public StreamCodec<RegistryFriendlyByteBuf, CraftingDisplay> streamCodec() {
        return CODEC;
    }
}
