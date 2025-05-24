package org.leavesmc.leaves.protocol.rei.display;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import org.bukkit.craftbukkit.CraftRegistry;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.rei.ingredient.EntryIngredient;

import java.util.List;
import java.util.Optional;

/**
 * see me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapedDisplay#DefaultShapedDisplay(RecipeHolder)
 */
public class ShapedDisplay extends CraftingDisplay {
    private static final StreamCodec<RegistryFriendlyByteBuf, CraftingDisplay> CODEC = StreamCodec.composite(
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        CraftingDisplay::getInputEntries,
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        CraftingDisplay::getOutputEntries,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
        CraftingDisplay::getOptionalLocation,
        ByteBufCodecs.INT,
        CraftingDisplay::getWidth,
        ByteBufCodecs.INT,
        CraftingDisplay::getHeight,
        ShapedDisplay::of
    );
    private static final ResourceLocation SERIALIZER_ID = ResourceLocation.tryBuild("minecraft", "default/crafting/shaped");
    private final int width;
    private final int height;

    public ShapedDisplay(@NotNull RecipeHolder<ShapedRecipe> recipeHolder) {
        super(
            ofIngredient(recipeHolder.value()),
            List.of(EntryIngredient.of(recipeHolder.value().assemble(CraftingInput.EMPTY, CraftRegistry.getMinecraftRegistry()))),
            recipeHolder.id().location()
        );
        this.width = recipeHolder.value().getWidth();
        this.height = recipeHolder.value().getHeight();
    }

    public ShapedDisplay(@NotNull ShapedCraftingRecipeDisplay recipeDisplay, ResourceLocation id) {
        super(
            Display.ofSlotDisplays(recipeDisplay.ingredients()),
            List.of(ofSlotDisplay(recipeDisplay.result())),
            id
        );
        this.width = recipeDisplay.width();
        this.height = recipeDisplay.height();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static CraftingDisplay of(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location, int width, int height) {
        throw new UnsupportedOperationException();
    }

    private static List<EntryIngredient> ofIngredient(ShapedRecipe recipe) {
        return recipe.getIngredients().stream().map(ingredient -> {
            if (ingredient.isEmpty()) {
                return EntryIngredient.empty();
            }
            ItemStack[] itemStacks = ingredient.get().items()
                .map(itemHolder -> new ItemStack(itemHolder, 1))
                .toArray(ItemStack[]::new);
            return EntryIngredient.of(itemStacks);
        }).toList();
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

    public StreamCodec<RegistryFriendlyByteBuf, CraftingDisplay> streamCodec() {
        return CODEC;
    }
}
