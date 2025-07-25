package org.leavesmc.leaves.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ItemOverstackUtils {

    private static final List<ItemUtil> overstackUtils = List.of(
        new ShulkerBox(),
        new CurseEnchantedBook()
    );

    public static int getItemStackMaxCount(ItemStack stack) {
        int size;
        for (ItemUtil util : overstackUtils) {
            if ((size = util.getMaxServerStackCount(stack)) != -1) {
                return size;
            }
        }
        return stack.getMaxStackSize();
    }

    public static int getNetworkMaxCount(ItemStack stack) {
        int size;
        for (ItemUtil util : overstackUtils) {
            if ((size = util.getMaxClientStackCount(stack)) != -1) {
                return size;
            }
        }
        return stack.getMaxStackSize();
    }

    public static boolean tryStackItems(ItemEntity self, ItemEntity other) {
        for (ItemUtil util : overstackUtils) {
            if (util.tryStackItems(self, other)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasOverstackingItem() {
        return overstackUtils.stream().anyMatch(ItemUtil::isEnabled);
    }

    public static int getItemStackMaxCountReal(ItemStack stack) {
        CompoundTag nbt = Optional.ofNullable(stack.get(DataComponents.CUSTOM_DATA)).orElse(CustomData.EMPTY).copyTag();
        return nbt.getInt("Leaves.RealStackSize").orElse(stack.getMaxStackSize());
    }

    public static ItemStack encodeMaxStackSize(ItemStack itemStack) {
        int realMaxStackSize = getItemStackMaxCountReal(itemStack);
        int modifiedMaxStackSize = getNetworkMaxCount(itemStack);
        if (itemStack.getMaxStackSize() != modifiedMaxStackSize) {
            itemStack.set(DataComponents.MAX_STACK_SIZE, modifiedMaxStackSize);
            CompoundTag nbt = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            nbt.putInt("Leaves.RealStackSize", realMaxStackSize);
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        }
        return itemStack;
    }

    public static ItemStack decodeMaxStackSize(ItemStack itemStack) {
        int realMaxStackSize = getItemStackMaxCountReal(itemStack);
        if (itemStack.getMaxStackSize() != realMaxStackSize) {
            itemStack.set(DataComponents.MAX_STACK_SIZE, realMaxStackSize);
            CompoundTag nbt = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            nbt.remove("Leaves.RealStackSize");
            if (nbt.isEmpty()) {
                itemStack.remove(DataComponents.CUSTOM_DATA);
            } else {
                itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            }
        }
        return itemStack;
    }

    public static boolean isStackable(ItemStack itemStack) {
        return getItemStackMaxCount(itemStack) > 1 && (!itemStack.isDamageableItem() || !itemStack.isDamaged());
    }


    private interface ItemUtil {
        boolean isEnabled();

        boolean tryStackItems(ItemEntity self, ItemEntity other);

        // number -> modified count, -1 -> I don't care
        int getMaxServerStackCount(ItemStack stack);

        // number -> modified count, -1 -> I don't care
        default int getMaxClientStackCount(ItemStack stack) {
            return getMaxServerStackCount(stack);
        }
    }

    private static class ShulkerBox implements ItemUtil {
        public static boolean shulkerBoxCheck(@NotNull ItemStack stack1, @NotNull ItemStack stack2) {
            if (LeavesConfig.modify.shulkerBox.sameNbtStackable) {
                return Objects.equals(stack1.getComponents(), stack2.getComponents());
            }
            return shulkerBoxNoItem(stack1) && shulkerBoxNoItem(stack2) && Objects.equals(stack1.getComponents(), stack2.getComponents());
        }

        public static boolean shulkerBoxNoItem(@NotNull ItemStack stack) {
            return stack.getComponents().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).stream().findAny().isEmpty();
        }

        @Override
        public boolean isEnabled() {
            return LeavesConfig.modify.shulkerBox.shulkerBoxStackSize > 1;
        }

        @Override
        public boolean tryStackItems(ItemEntity self, ItemEntity other) {
            ItemStack selfStack = self.getItem();
            if (!isEnabled() ||
                !(selfStack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) ||
                !(blockItem.getBlock() instanceof net.minecraft.world.level.block.ShulkerBoxBlock)
            ) {
                return false;
            }

            ItemStack otherStack = other.getItem();
            if (selfStack.getItem() == otherStack.getItem()
                && shulkerBoxCheck(selfStack, otherStack)
                && selfStack.getCount() != org.leavesmc.leaves.LeavesConfig.modify.shulkerBox.shulkerBoxStackSize) {
                int amount = Math.min(otherStack.getCount(), org.leavesmc.leaves.LeavesConfig.modify.shulkerBox.shulkerBoxStackSize - selfStack.getCount());

                selfStack.grow(amount);
                self.setItem(selfStack);

                self.pickupDelay = Math.max(other.pickupDelay, self.pickupDelay);
                self.age = Math.min(other.getAge(), self.age);

                otherStack.shrink(amount);
                if (otherStack.isEmpty()) {
                    other.discard();
                } else {
                    other.setItem(otherStack);
                }
                return true;
            }
            return false;
        }

        @Override
        public int getMaxServerStackCount(ItemStack stack) {
            if (isEnabled() && stack.getItem() instanceof BlockItem bi &&
                bi.getBlock() instanceof ShulkerBoxBlock && (LeavesConfig.modify.shulkerBox.sameNbtStackable || shulkerBoxNoItem(stack))) {
                return LeavesConfig.modify.shulkerBox.shulkerBoxStackSize;
            }
            return -1;
        }
    }

    public static class CurseEnchantedBook implements ItemUtil {
        public static boolean isCursedEnchantedBook(ItemStack stack) {
            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (enchantments.size() != 1) {
                return false;
            }
            return stack.getBukkitStack().getEnchantmentLevel(org.bukkit.enchantments.Enchantment.BINDING_CURSE) == 1 ||
                stack.getBukkitStack().getEnchantmentLevel(org.bukkit.enchantments.Enchantment.BINDING_CURSE) == 1;
        }

        @Override
        public boolean isEnabled() {
            return LeavesConfig.modify.oldMC.allowGrindstoneOverstacking;
        }

        @Override
        public boolean tryStackItems(ItemEntity self, ItemEntity other) {
            return false;
        }

        @Override
        public int getMaxServerStackCount(ItemStack stack) {
            return -1;
        }

        @Override
        public int getMaxClientStackCount(ItemStack stack) {
            if (isEnabled() && isCursedEnchantedBook(stack)) {
                return 2;
            }
            return -1;
        }
    }
}