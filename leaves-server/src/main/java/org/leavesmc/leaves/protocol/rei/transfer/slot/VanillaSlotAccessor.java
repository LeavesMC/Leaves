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

package org.leavesmc.leaves.protocol.rei.transfer.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class VanillaSlotAccessor implements SlotAccessor {
    protected Slot slot;

    public VanillaSlotAccessor(Slot slot) {
        this.slot = slot;
    }

    @Override
    public ItemStack getItemStack() {
        return slot.getItem();
    }

    @Override
    public void setItemStack(ItemStack stack) {
        this.slot.set(stack);
    }

    @Override
    public void takeStack(int amount) {
        slot.remove(amount);
    }

    public Slot getSlot() {
        return slot;
    }

    @Override
    public boolean allowModification(Player player) {
        return slot.allowModification(player);
    }

    @Override
    public boolean canPlace(ItemStack stack) {
        return slot.mayPlace(stack);
    }
}