package org.leavesmc.leaves.protocol.rei.display;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.bukkit.craftbukkit.CraftRegistry;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.rei.ingredient.EntryIngredient;

import java.util.List;
import java.util.Optional;

public abstract class CookingDisplay extends Display {
    private static final StreamCodec<RegistryFriendlyByteBuf, CookingDisplay> CODEC = StreamCodec.composite(
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        CookingDisplay::getInputEntries,
        EntryIngredient.CODEC.apply(ByteBufCodecs.list()),
        CookingDisplay::getOutputEntries,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
        CookingDisplay::getOptionalLocation,
        ByteBufCodecs.FLOAT,
        CookingDisplay::getXp,
        ByteBufCodecs.DOUBLE,
        CookingDisplay::getCookTime,
        CookingDisplay::of
    );
    protected float xp;
    protected double cookTime;

    private CookingDisplay(@NotNull List<EntryIngredient> inputs, @NotNull List<EntryIngredient> outputs, @NotNull ResourceLocation id, float xp, double cookTime) {
        super(inputs, outputs, id);
        this.xp = xp;
        this.cookTime = cookTime;
    }

    public CookingDisplay(RecipeHolder<? extends AbstractCookingRecipe> recipe) {
        this(
            List.of(EntryIngredient.ofIngredient(recipe.value().input())),
            List.of(EntryIngredient.of(recipe.value().assemble(new SingleRecipeInput(ItemStack.EMPTY), CraftRegistry.getMinecraftRegistry()))),
            recipe.id().location(),
            recipe.value().experience(),
            recipe.value().cookingTime()
        );
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static CookingDisplay of(@NotNull List<EntryIngredient> inputs, @NotNull List<EntryIngredient> outputs, @NotNull Optional<ResourceLocation> id, float xp, double cookTime) {
        throw new UnsupportedOperationException();
    }

    public float getXp() {
        return xp;
    }

    public double getCookTime() {
        return cookTime;
    }

    public StreamCodec<RegistryFriendlyByteBuf, CookingDisplay> streamCodec() {
        return CODEC;
    }
}
