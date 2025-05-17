package org.leavesmc.leaves.bot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

// Power by gugle-carpet-addition(https://github.com/Gu-ZT/gugle-carpet-addition)
public class BotInventoryContainer extends Inventory {

    private final NonNullList<ItemStack> buttons = NonNullList.withSize(13, ItemStack.EMPTY);
    private final ServerBot player;

    public BotInventoryContainer(ServerBot player) {
        super(player, new EntityEquipment());
        this.player = player;
        createButton();
    }

    @Override
    public int getContainerSize() {
        return super.getContainerSize() + this.buttons.size();
    }

    @Override
    @Nonnull
    public ItemStack getItem(int slot) {
        Pair<List<ItemStack>, Integer> pair = getItemSlot(slot);
        if (pair != null) {
            return pair.getFirst().get(pair.getSecond());
        } else {
            return ItemStack.EMPTY;
        }
    }

    public Pair<List<ItemStack>, Integer> getItemSlot(int slot) {
        switch (slot) {
            case 0 -> {
                return new Pair<>(buttons, 0);
            }
            case 1, 2, 3, 4 -> {
                return new Pair<>(super.getArmorContents(), 4 - slot);
            }
            case 5, 6 -> {
                return new Pair<>(buttons, slot - 4);
            }
            case 7 -> {
                return new Pair<>(Collections.singletonList(super.equipment.get(EquipmentSlot.OFFHAND)), 0);
            }
            case 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 -> {
                return new Pair<>(buttons, slot - 5);
            }
            case 18, 19, 20, 21, 22, 23, 24, 25, 26,
                 27, 28, 29, 30, 31, 32, 33, 34, 35,
                 36, 37, 38, 39, 40, 41, 42, 43, 44 -> {
                return new Pair<>(super.getNonEquipmentItems(), slot - 9);
            }
            case 45, 46, 47, 48, 49, 50, 51, 52, 53 -> {
                return new Pair<>(super.getNonEquipmentItems(), slot - 45);
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    @Nonnull
    public ItemStack removeItem(int slot, int amount) {
        Pair<List<ItemStack>, Integer> pair = getItemSlot(slot);
        List<ItemStack> list = null;
        ItemStack itemStack = ItemStack.EMPTY;
        if (pair != null) {
            list = pair.getFirst();
            slot = pair.getSecond();
        }
        if (list != null && !list.get(slot).isEmpty()) {
            itemStack = ContainerHelper.removeItem(list, slot, amount);
            player.detectEquipmentUpdates();
        }
        return itemStack;
    }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int slot) {
        Pair<List<ItemStack>, Integer> pair = getItemSlot(slot);
        List<ItemStack> list = null;
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
        Pair<List<ItemStack>, Integer> pair = getItemSlot(slot);
        List<ItemStack> list = null;
        if (pair != null) {
            list = pair.getFirst();
            slot = pair.getSecond();
        }
        if (list != null) {
            list.set(slot, stack);
            player.detectEquipmentUpdates();
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