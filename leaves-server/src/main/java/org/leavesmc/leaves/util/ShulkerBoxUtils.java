package org.leavesmc.leaves.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;

public class ShulkerBoxUtils {

    public static boolean shulkerBoxNoItem(@NotNull ItemStack stack) {
        return stack.getComponents().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).stream().findAny().isEmpty();
    }

    public static int getItemStackMaxCount(ItemStack stack) {
        if (LeavesConfig.modify.shulkerBoxStackSize > 1 && stack.getItem() instanceof BlockItem bi &&
            bi.getBlock() instanceof ShulkerBoxBlock && shulkerBoxNoItem(stack)) {
            return LeavesConfig.modify.shulkerBoxStackSize;
        }
        return stack.getMaxStackSize();
    }

    public static ItemStack correctItemStackMaxStackSize(ItemStack itemStack) {
        int trulyMaxStackSize = getItemStackMaxCount(itemStack);
        if (itemStack.getMaxStackSize() != trulyMaxStackSize) {
            org.bukkit.inventory.ItemStack bkStack = CraftItemStack.asBukkitCopy(itemStack);
            bkStack.editMeta(meta -> meta.setMaxStackSize(trulyMaxStackSize));
            itemStack = CraftItemStack.asNMSCopy(bkStack);
        }
        return itemStack;
    }

    public static boolean isStackable(ItemStack itemStack) {
        return getItemStackMaxCount(itemStack) > 1 && (!itemStack.isDamageableItem() || !itemStack.isDamaged());
    }
}
