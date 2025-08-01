/*
 * This file is part of Lithium
 *
 * Lithium is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lithium is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Lithium. If not, see <https://www.gnu.org/licenses/>.
 */

package org.leavesmc.leaves.lithium.common.hopper;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.leavesmc.leaves.lithium.api.inventory.LithiumInventory;
import org.leavesmc.leaves.lithium.common.block.entity.inventory_change_tracking.InventoryChangeEmitter;
import org.leavesmc.leaves.lithium.common.block.entity.inventory_change_tracking.InventoryChangeListener;
import org.leavesmc.leaves.lithium.common.block.entity.inventory_change_tracking.InventoryChangeTracker;
import org.leavesmc.leaves.lithium.common.block.entity.inventory_comparator_tracking.ComparatorTracker;

public class LithiumDoubleInventory extends CompoundContainer implements LithiumInventory, InventoryChangeTracker, InventoryChangeEmitter, InventoryChangeListener, ComparatorTracker {

    private final LithiumInventory first;
    private final LithiumInventory second;

    private LithiumStackList doubleStackList;

    ReferenceOpenHashSet<InventoryChangeListener> inventoryChangeListeners = null;
    ReferenceOpenHashSet<InventoryChangeListener> inventoryHandlingTypeListeners = null;

    /**
     * This method returns the same LithiumDoubleInventory instance for equal (same children in same order)
     * doubleInventory parameters until {@link #lithium$emitRemoved()} is called. After that a new LithiumDoubleInventory object
     * may be in use.
     *
     * @param doubleInventory A double inventory
     * @return The only non-removed LithiumDoubleInventory instance for the double inventory. Null if not compatible
     */
    public static LithiumDoubleInventory getLithiumInventory(CompoundContainer doubleInventory) {
        Container vanillaFirst = doubleInventory.container1;
        Container vanillaSecond = doubleInventory.container2;
        if (vanillaFirst != vanillaSecond && vanillaFirst instanceof LithiumInventory first && vanillaSecond instanceof LithiumInventory second) {
            LithiumDoubleInventory newDoubleInventory = new LithiumDoubleInventory(first, second);
            LithiumDoubleStackList doubleStackList = LithiumDoubleStackList.getOrCreate(
                newDoubleInventory,
                InventoryHelper.getLithiumStackList(first),
                InventoryHelper.getLithiumStackList(second),
                newDoubleInventory.getMaxStackSize()
            );
            newDoubleInventory.doubleStackList = doubleStackList;
            return doubleStackList.doubleInventory;
        }
        return null;
    }

    private LithiumDoubleInventory(LithiumInventory first, LithiumInventory second) {
        super(first, second);
        this.first = first;
        this.second = second;
    }

    @Override
    public void lithium$emitContentModified() {
        ReferenceOpenHashSet<InventoryChangeListener> inventoryChangeListeners = this.inventoryChangeListeners;
        if (inventoryChangeListeners != null) {
            for (InventoryChangeListener inventoryChangeListener : inventoryChangeListeners) {
                inventoryChangeListener.lithium$handleInventoryContentModified(this);
            }
            inventoryChangeListeners.clear();
        }
    }

    @Override
    public void lithium$emitStackListReplaced() {
        ReferenceOpenHashSet<InventoryChangeListener> listeners = this.inventoryHandlingTypeListeners;
        if (listeners != null && !listeners.isEmpty()) {
            listeners.forEach(inventoryChangeListener -> inventoryChangeListener.handleStackListReplaced(this));
        }

        this.invalidateChangeListening();
    }

    @Override
    public void lithium$emitRemoved() {
        ReferenceOpenHashSet<InventoryChangeListener> listeners = this.inventoryHandlingTypeListeners;
        if (listeners != null && !listeners.isEmpty()) {
            listeners.forEach(listener -> listener.lithium$handleInventoryRemoved(this));
        }

        this.invalidateChangeListening();
    }

    private void invalidateChangeListening() {
        if (this.inventoryChangeListeners != null) {
            this.inventoryChangeListeners.clear();
        }

        LithiumStackList lithiumStackList = InventoryHelper.getLithiumStackListOrNull(this);
        if (lithiumStackList != null) {
            lithiumStackList.removeInventoryModificationCallback(this);
        }
    }

    @Override
    public void lithium$emitFirstComparatorAdded() {
        ReferenceOpenHashSet<InventoryChangeListener> inventoryChangeListeners = this.inventoryChangeListeners;
        if (inventoryChangeListeners != null && !inventoryChangeListeners.isEmpty()) {
            inventoryChangeListeners.removeIf(inventoryChangeListener -> inventoryChangeListener.lithium$handleComparatorAdded(this));
        }
    }

    @Override
    public void lithium$forwardContentChangeOnce(InventoryChangeListener inventoryChangeListener, LithiumStackList stackList, InventoryChangeTracker thisTracker) {
        if (this.inventoryChangeListeners == null) {
            this.inventoryChangeListeners = new ReferenceOpenHashSet<>(1);
        }
        stackList.setInventoryModificationCallback(thisTracker);
        this.inventoryChangeListeners.add(inventoryChangeListener);

    }

    @Override
    public void lithium$forwardMajorInventoryChanges(InventoryChangeListener inventoryChangeListener) {
        if (this.inventoryHandlingTypeListeners == null) {
            this.inventoryHandlingTypeListeners = new ReferenceOpenHashSet<>(1);

            ((InventoryChangeTracker) this.first).listenForMajorInventoryChanges(this);
            ((InventoryChangeTracker) this.second).listenForMajorInventoryChanges(this);
        }
        this.inventoryHandlingTypeListeners.add(inventoryChangeListener);
    }

    @Override
    public void lithium$stopForwardingMajorInventoryChanges(InventoryChangeListener inventoryChangeListener) {
        if (this.inventoryHandlingTypeListeners != null) {
            this.inventoryHandlingTypeListeners.remove(inventoryChangeListener);
            if (this.inventoryHandlingTypeListeners.isEmpty()) {
                ((InventoryChangeTracker) this.first).stopListenForMajorInventoryChanges(this);
                ((InventoryChangeTracker) this.second).stopListenForMajorInventoryChanges(this);
            }
        }
    }

    @Override
    public NonNullList<ItemStack> getInventoryLithium() {
        return this.doubleStackList;
    }

    @Override
    public void setInventoryLithium(NonNullList<ItemStack> inventory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void lithium$handleInventoryContentModified(Container inventory) {
        this.lithium$emitContentModified();
    }

    @Override
    public void lithium$handleInventoryRemoved(Container inventory) {
        this.lithium$emitRemoved();
    }

    @Override
    public boolean lithium$handleComparatorAdded(Container inventory) {
        this.lithium$emitFirstComparatorAdded();
        return this.inventoryChangeListeners.isEmpty();
    }

    @Override
    public void lithium$onComparatorAdded(Direction direction, int offset) {
        throw new UnsupportedOperationException("Call onComparatorAdded(Direction direction, int offset) on the inventory half only!");
    }

    @Override
    public boolean lithium$hasAnyComparatorNearby() {
        return ((ComparatorTracker) this.first).lithium$hasAnyComparatorNearby() || ((ComparatorTracker) this.second).lithium$hasAnyComparatorNearby();
    }
}