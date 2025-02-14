package org.leavesmc.leaves.bot;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nonnull;
import java.util.List;

// Power by gugle-carpet-addition(https://github.com/Gu-ZT/gugle-carpet-addition)
public class BotInventoryContainer extends SimpleContainer {

    public final NonNullList<ItemStack> items;
    public final NonNullList<ItemStack> armor;
    public final NonNullList<ItemStack> offhand;
    private final List<NonNullList<ItemStack>> compartments;
    private final NonNullList<ItemStack> buttons = NonNullList.withSize(13, ItemStack.EMPTY);
    private final ServerBot player;

    public BotInventoryContainer(ServerBot player) {
        this.player = player;
        this.items = this.player.getInventory().items;
        this.armor = this.player.getInventory().armor;
        this.offhand = this.player.getInventory().offhand;
        this.compartments = ImmutableList.of(this.items, this.armor, this.offhand, this.buttons);
        createButton();
    }

    @Override
    public int getContainerSize() {
        return this.items.size() + this.armor.size() + this.offhand.size() + this.buttons.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.items) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        for (ItemStack itemStack : this.armor) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        for (ItemStack itemStack : this.offhand) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getItem(int slot) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        if (pair != null) {
            return pair.getFirst().get(pair.getSecond());
        } else {
            return ItemStack.EMPTY;
        }
    }

    public Pair<NonNullList<ItemStack>, Integer> getItemSlot(int slot) {
        switch (slot) {
            case 0 -> {
                return new Pair<>(buttons, 0);
            }
            case 1, 2, 3, 4 -> {
                return new Pair<>(armor, 4 - slot);
            }
            case 5, 6 -> {
                return new Pair<>(buttons, slot - 4);
            }
            case 7 -> {
                return new Pair<>(offhand, 0);
            }
            case 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 -> {
                return new Pair<>(buttons, slot - 5);
            }
            case 18, 19, 20, 21, 22, 23, 24, 25, 26,
                 27, 28, 29, 30, 31, 32, 33, 34, 35,
                 36, 37, 38, 39, 40, 41, 42, 43, 44 -> {
                return new Pair<>(items, slot - 9);
            }
            case 45, 46, 47, 48, 49, 50, 51, 52, 53 -> {
                return new Pair<>(items, slot - 45);
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    @Nonnull
    public ItemStack removeItem(int slot, int amount) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        ItemStack itemStack = ItemStack.EMPTY;
        if (pair != null) {
            list = pair.getFirst();
            slot = pair.getSecond();
        }
        if (list != null && !list.get(slot).isEmpty()) {
            itemStack = ContainerHelper.removeItem(list, slot, amount);
            player.detectEquipmentUpdatesPublic();
        }
        return itemStack;
    }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int slot) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        if (pair != null) {
            list = pair.getFirst();
            slot = pair.getSecond();
        }
        if (list != null && !list.get(slot).isEmpty()) {
            ItemStack itemStack = list.get(slot);
            list.set(slot, ItemStack.EMPTY);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, @Nonnull ItemStack stack) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        if (pair != null) {
            list = pair.getFirst();
            slot = pair.getSecond();
        }
        if (list != null) {
            list.set(slot, stack);
            player.detectEquipmentUpdatesPublic();
        }
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

    @Override
    public void clearContent() {
        for (List<ItemStack> list : this.compartments) {
            list.clear();
        }
    }

    private void createButton() {
        CompoundTag customData = new CompoundTag();
        customData.putBoolean("Leaves.Gui.Placeholder", true);

        DataComponentPatch patch = DataComponentPatch.builder()
            .set(DataComponents.CUSTOM_NAME, Component.empty())
            .set(DataComponents.CUSTOM_DATA, CustomData.of(customData))
            .build();

        for (int i = 0; i < 13; i++) {
            ItemStack button = new ItemStack(Items.STRUCTURE_VOID);
            button.applyComponents(patch);
            buttons.set(i, button);
        }
    }
}
