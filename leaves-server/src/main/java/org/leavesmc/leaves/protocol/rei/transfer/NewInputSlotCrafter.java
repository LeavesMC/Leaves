package org.leavesmc.leaves.protocol.rei.transfer;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.leavesmc.leaves.protocol.rei.transfer.slot.SlotAccessor;

import java.util.HashMap;
import java.util.List;

public class NewInputSlotCrafter<T extends AbstractContainerMenu> extends InputSlotCrafter<T> {
    protected final List<SlotAccessor> inputSlots;
    protected final List<SlotAccessor> inventorySlots;
    protected final List<List<ItemStack>> inputs;

    public NewInputSlotCrafter(T container, List<SlotAccessor> inputSlots, List<SlotAccessor> inventorySlots, List<List<ItemStack>> inputs) {
        super(container);
        this.inputSlots = inputSlots;
        this.inventorySlots = inventorySlots;
        this.inputs = inputs;
    }

    @Override
    protected Iterable<SlotAccessor> getInputSlots() {
        return this.inputSlots;
    }

    @Override
    protected Iterable<SlotAccessor> getInventorySlots() {
        return this.inventorySlots;
    }

    @Override
    protected List<List<ItemStack>> getInputs() {
        return this.inputs;
    }

    @Override
    protected void populateRecipeFinder(ItemRecipeFinder recipeFinder) {
        for (SlotAccessor slot : getInventorySlots()) {
            recipeFinder.addNormalItem(slot.getItemStack());
        }
    }

    @Override
    protected void markDirty() {
        player.getInventory().setChanged();
        container.sendAllDataToRemote();
    }

    @Override
    protected void cleanInputs() {
        for (SlotAccessor slot : getInputSlots()) {
            org.bukkit.inventory.ItemStack bukkitStack = slot.getItemStack().getBukkitStack();
            if (bukkitStack.getType().isAir()) {
                continue;
            }
            HashMap<Integer, org.bukkit.inventory.ItemStack> notAdded = player.getBukkitEntity().getInventory().addItem(bukkitStack);
            if (notAdded.isEmpty()) {
                slot.setItemStack(ItemStack.EMPTY);
            } else {
                org.bukkit.inventory.ItemStack remain = notAdded.values().iterator().next();
                slot.setItemStack(ItemStack.fromBukkitCopy(remain));
                throw new IllegalStateException("rei.rei.no.slot.in.inv");
            }
        }
    }
}