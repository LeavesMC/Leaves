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

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.protocol.rei.transfer.slot.SlotAccessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class InputSlotCrafter<T extends AbstractContainerMenu> {
    protected T container;
    private Iterable<SlotAccessor> inputStacks;
    private Iterable<SlotAccessor> inventoryStacks;
    protected ServerPlayer player;

    protected InputSlotCrafter(T container) {
        this.container = container;
    }

    public void fillInputSlots(ServerPlayer player, boolean hasShift) {
        this.player = player;
        this.inventoryStacks = this.getInventorySlots();
        this.inputStacks = this.getInputSlots();

        // Return the already placed items on the grid
        this.cleanInputs();

        ItemRecipeFinder recipeFinder = new ItemRecipeFinder();
        this.populateRecipeFinder(recipeFinder);
        List<List<ItemStack>> ingredients = new ArrayList<>(this.getInputs());

        if (recipeFinder.findRecipe(ingredients, 1, null)) {
            this.fillInputSlots(recipeFinder, ingredients, hasShift);
        } else {
            this.cleanInputs();
            this.markDirty();
            throw new NotEnoughMaterialsException();
        }

        this.markDirty();
    }

    protected abstract Iterable<SlotAccessor> getInputSlots();

    protected abstract Iterable<SlotAccessor> getInventorySlots();

    protected abstract List<List<ItemStack>> getInputs();

    protected abstract void populateRecipeFinder(ItemRecipeFinder recipeFinder);

    protected abstract void markDirty();

    public void alignRecipeToGrid(Iterable<SlotAccessor> inputStacks, Iterator<ItemStack> recipeItems, int craftsAmount) {
        for (SlotAccessor inputStack : inputStacks) {
            if (!recipeItems.hasNext()) {
                return;
            }

            this.acceptAlignedInput(recipeItems.next(), inputStack, craftsAmount);
        }
    }

    public void acceptAlignedInput(ItemStack toBeTakenStack, SlotAccessor inputStack, int craftsAmount) {
        if (!toBeTakenStack.isEmpty()) {
            for (int i = 0; i < craftsAmount; ++i) {
                this.fillInputSlot(inputStack, toBeTakenStack);
            }
        }
    }

    protected void fillInputSlot(SlotAccessor slot, ItemStack toBeTakenStack) {
        SlotAccessor takenSlot = this.takeInventoryStack(toBeTakenStack);
        if (takenSlot != null) {
            ItemStack takenStack = takenSlot.getItemStack().copy();
            if (!takenStack.isEmpty()) {
                if (takenStack.getCount() > 1) {
                    takenSlot.takeStack(1);
                } else {
                    takenSlot.setItemStack(ItemStack.EMPTY);
                }

                takenStack.setCount(1);
                if (!slot.canPlace(takenStack)) {
                    return;
                }

                if (slot.getItemStack().isEmpty()) {
                    slot.setItemStack(takenStack);
                } else {
                    slot.getItemStack().grow(1);
                }
            }
        }
    }

    protected void fillInputSlots(ItemRecipeFinder recipeFinder, List<List<ItemStack>> ingredients, boolean hasShift) {
        int recipeCrafts = recipeFinder.countRecipeCrafts(ingredients, Integer.MAX_VALUE, null);
        int amountToFill = hasShift ? recipeCrafts : 1;
        List<ItemStack> recipeItems = new ArrayList<>();
        if (recipeFinder.findRecipe(ingredients, amountToFill, recipeItems::add)) {
            int finalCraftsAmount = amountToFill;

            for (ItemStack itemId : recipeItems) {
                // Fix issue with empty item id (grid slot) [shift-click issue]
                if (itemId.isEmpty()) {
                    continue;
                }
                finalCraftsAmount = Math.min(finalCraftsAmount, itemId.getMaxStackSize());
            }

            recipeItems.clear();

            if (recipeFinder.findRecipe(ingredients, finalCraftsAmount, recipeItems::add)) {
                this.cleanInputs();
                this.alignRecipeToGrid(inputStacks, recipeItems.iterator(), finalCraftsAmount);
            }
        }
    }

    protected abstract void cleanInputs();

    @Nullable
    public SlotAccessor takeInventoryStack(ItemStack itemStack) {
        boolean rejectedModification = false;
        for (SlotAccessor inventoryStack : inventoryStacks) {
            ItemStack itemStack1 = inventoryStack.getItemStack();
            if (!itemStack1.isEmpty() && areItemsEqual(itemStack, itemStack1) && !itemStack1.isDamaged() && !itemStack1.isEnchanted() && !itemStack1.has(DataComponents.CUSTOM_NAME)) {
                if (!inventoryStack.allowModification(player)) {
                    rejectedModification = true;
                } else {
                    return inventoryStack;
                }
            }
        }

        if (rejectedModification) {
            throw new IllegalStateException("Unable to take item from inventory due to slot not allowing modification! Item requested: " + itemStack);
        }

        return null;
    }

    private static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItemSameComponents(stack1, stack2);
    }

    public static class NotEnoughMaterialsException extends RuntimeException {
    }
}