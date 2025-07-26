/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.leavesmc.leaves.protocol.rei.transfer;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ItemRecipeFinder {
    private final Interner<ItemKey> keys = Interners.newWeakInterner();
    private final RecipeFinder<ItemKey, Ingredient> finder = new RecipeFinder<>();

    public boolean contains(ItemStack item) {
        return finder.contains(ofKey(item));
    }

    public void take(ItemStack item, int amount) {
        finder.take(ofKey(item), amount);
    }

    public void put(ItemStack item, int amount) {
        finder.put(ofKey(item), amount);
    }

    public void addNormalItem(ItemStack itemStack) {
        if (Inventory.isUsableForCrafting(itemStack)) {
            this.addItem(itemStack);
        }
    }

    public void addItem(ItemStack itemStack) {
        this.addItem(itemStack, itemStack.getMaxStackSize());
    }

    public void addItem(ItemStack itemStack, int i) {
        if (!itemStack.isEmpty()) {
            int j = Math.min(i, itemStack.getCount());
            this.finder.put(ofKey(itemStack), j);
        }
    }

    public boolean findRecipe(List<List<ItemStack>> list, int maxCrafts, @Nullable Consumer<ItemStack> output) {
        return finder.findRecipe(toIngredients(list), maxCrafts, flatten(itemStack -> {
            if (output != null) {
                output.accept(itemStack);
            }
        }));
    }

    public int countRecipeCrafts(List<List<ItemStack>> list, int maxCrafts, @Nullable Consumer<ItemStack> output) {
        return finder.countRecipeCrafts(toIngredients(list), maxCrafts, flatten(itemStack -> {
            if (output != null) {
                output.accept(itemStack);
            }
        }));
    }

    private ItemKey ofKey(ItemStack itemStack) {
        return keys.intern(new ItemKey(itemStack.getItemHolder(), itemStack.getComponentsPatch()));
    }

    private Ingredient ofKeys(int index, List<ItemStack> itemStack) {
        return new Ingredient(index, itemStack.stream().map(this::ofKey).toList());
    }

    private List<Ingredient> toIngredients(List<List<ItemStack>> list) {
        List<Ingredient> ingredients = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            List<ItemStack> stacks = list.get(i);
            if (!stacks.isEmpty()) {
                ingredients.add(ofKeys(i, stacks));
            }
        }

        return ingredients;
    }

    private static BiConsumer<ItemKey, Ingredient> flatten(Consumer<ItemStack> consumer) {
        int[] lastIndex = {-1};
        return (itemKey, ingredient) -> {
            for (int i = lastIndex[0] + 1; i < ingredient.index(); i++) {
                consumer.accept(ItemStack.EMPTY);
            }
            consumer.accept(new ItemStack(itemKey.item(), 1, itemKey.patch()));
            lastIndex[0] = ingredient.index();
        };
    }

    private record Ingredient(int index, List<ItemKey> elements) implements RecipeFinder.Ingredient<ItemKey> {
    }

    private record ItemKey(Holder<Item> item, DataComponentPatch patch) {
    }
}