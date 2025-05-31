package org.leavesmc.leaves.bot;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nonnull;

public class BotInventoryContainer extends Inventory {

    private static final ItemStack button;

    static {
        CompoundTag customData = new CompoundTag();
        customData.putBoolean("Leaves.Gui.Placeholder", true);

        DataComponentPatch patch = DataComponentPatch.builder()
            .set(DataComponents.CUSTOM_NAME, Component.empty())
            .set(DataComponents.CUSTOM_DATA, CustomData.of(customData))
            .build();

        button = new ItemStack(Items.STRUCTURE_VOID);
        button.applyComponents(patch);
    }

    private final Inventory original;

    public BotInventoryContainer(Inventory original) {
        super(original.player, original.equipment);
        this.original = original;
    }

    @Override
    public int getContainerSize() {
        return 54;
    }

    @Override
    @Nonnull
    public ItemStack getItem(int slot) {
        int realSlot = convertSlot(slot);
        if (realSlot == -999) {
            // buttons are the same
            return button;
        }
        return original.getItem(realSlot);
    }

    public int convertSlot(int slot) {
        return switch (slot) {
            // Mainhand is always store at slot 0
            case 6 -> 0;

            // Offhand
            case 7 -> 40;

            // Equipment slot start at 36
            case 1, 2, 3, 4 -> 40 - slot;

            // Inventory storage
            case 18, 19, 20, 21, 22, 23, 24, 25, 26,
                 27, 28, 29, 30, 31, 32, 33, 34, 35,
                 36, 37, 38, 39, 40, 41, 42, 43, 44 -> slot - 9;

            // Hotbar, 45 -> Mainhand (0)
            case 45, 46, 47, 48, 49, 50, 51, 52, 53 -> slot - 45;

            // Buttons
            default -> -999;
        };
    }

    @Override
    @Nonnull
    public ItemStack removeItem(int slot, int amount) {
        int realSlot = convertSlot(slot);
        if (realSlot == -999) {
            // Don't remove buttons
            return ItemStack.EMPTY;
        }
        ItemStack removed = original.removeItem(realSlot, amount);
        player.detectEquipmentUpdates();
        return removed;
    }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int slot) {
        int realSlot = convertSlot(slot);
        if (realSlot == -999) {
            // Don't remove buttons
            return ItemStack.EMPTY;
        }
        return original.removeItemNoUpdate(realSlot);
    }

    @Override
    public void setItem(int slot, @Nonnull ItemStack stack) {
        int realSlot = convertSlot(slot);
        if (realSlot == -999) {
            // Don't modify buttons
            return;
        }
        original.setItem(realSlot, stack);
        player.detectEquipmentUpdates();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        if (this.player.isRemoved()) {
            return false;
        }
        return !(player.distanceToSqr(this.player) > 64.0);
    }
}