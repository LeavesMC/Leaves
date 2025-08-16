package org.leavesmc.leaves.bot;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class BotRecipeBook extends ServerRecipeBook {

    public BotRecipeBook() {
        super(($, $1) -> {
        });
    }

    @Override
    public void add(@NotNull ResourceKey<Recipe<?>> recipe) {
    }

    @Override
    public void remove(@NotNull ResourceKey<Recipe<?>> recipe) {
    }

    @Override
    public boolean contains(@NotNull ResourceKey<Recipe<?>> recipe) {
        return false;
    }

    @Override
    public void removeHighlight(@NotNull ResourceKey<Recipe<?>> recipe) {
    }

    @Override
    public int addRecipes(@NotNull Collection<RecipeHolder<?>> recipes, @NotNull ServerPlayer player) {
        return 0;
    }

    @Override
    public int removeRecipes(@NotNull Collection<RecipeHolder<?>> recipes, @NotNull ServerPlayer player) {
        return 0;
    }

    @Override
    public void loadUntrusted(@NotNull Packed recipeBook, @NotNull Predicate<ResourceKey<Recipe<?>>> predicate) {
    }

    @Override
    public @NotNull Packed pack() {
        return new ServerRecipeBook.Packed(this.bookSettings.copy(), List.of(), List.of());
    }
}
