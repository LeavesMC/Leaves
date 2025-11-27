package org.leavesmc.leaves.bot;

import com.google.common.base.Charsets;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;

import java.util.Optional;
import java.util.UUID;

public class BotUtil {

    public static void replenishment(@NotNull ItemStack itemStack, NonNullList<ItemStack> itemStackList) {
        int count = itemStack.getMaxStackSize() / 2;
        if (itemStack.getCount() <= 8 && count > 8) {
            for (ItemStack itemStack1 : itemStackList) {
                if (itemStack1 == ItemStack.EMPTY || itemStack1 == itemStack) {
                    continue;
                }

                if (ItemStack.isSameItemSameComponents(itemStack1, itemStack)) {
                    if (itemStack1.getCount() > count) {
                        itemStack.setCount(itemStack.getCount() + count);
                        itemStack1.setCount(itemStack1.getCount() - count);
                    } else {
                        itemStack.setCount(itemStack.getCount() + itemStack1.getCount());
                        itemStack1.setCount(0);
                    }
                    break;
                }
            }
        }
    }

    public static void replaceTool(@NotNull EquipmentSlot slot, @NotNull ServerBot bot) {
        ItemStack itemStack = bot.getItemBySlot(slot);
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack1 = bot.getInventory().getItem(i);
            if (itemStack1 == ItemStack.EMPTY || itemStack1 == itemStack) {
                continue;
            }

            if (itemStack1.getItem().getClass() == itemStack.getItem().getClass() && !isDamage(itemStack1, 10)) {
                ItemStack itemStack2 = itemStack1.copy();
                bot.getInventory().setItem(i, itemStack);
                bot.setItemSlot(slot, itemStack2);
                return;
            }
        }

        for (int i = 0; i < 36; i++) {
            ItemStack itemStack1 = bot.getInventory().getItem(i);
            if (itemStack1 == ItemStack.EMPTY && itemStack1 != itemStack) {
                bot.getInventory().setItem(i, itemStack);
                bot.setItemSlot(slot, ItemStack.EMPTY);
                return;
            }
        }
    }

    public static boolean isDamage(@NotNull ItemStack item, int minDamage) {
        return item.isDamageableItem() && (item.getMaxDamage() - item.getDamageValue()) <= minDamage;
    }

    @NotNull
    public static UUID getBotUUID(@NotNull BotCreateState state) {
        return getBotUUID(state.fullName());
    }

    public static UUID getBotUUID(@NotNull String fullName) {
        return UUID.nameUUIDFromBytes(("Fakeplayer:" + fullName).getBytes(Charsets.UTF_8));
    }

    public static UUID getBotLevel(@NotNull String fullName, BotDataStorage botDataStorage) {
        UUID uuid = getBotUUID(fullName);
        Optional<CompoundTag> tagOptional = botDataStorage.read(uuid.toString());
        if (tagOptional.isEmpty()) {
            return null;
        }
        CompoundTag tag = tagOptional.get();
        Optional<Long> worldUUIDMost = tag.getLong("WorldUUIDMost");
        Optional<Long> worldUUIDLeast = tag.getLong("WorldUUIDLeast");
        if (worldUUIDMost.isEmpty() || worldUUIDLeast.isEmpty()) {
            return null;
        }
        return new UUID(worldUUIDMost.get(), worldUUIDLeast.get());
    }

    public static String getFullName(String inputName) {
        return LeavesConfig.modify.fakeplayer.prefix + inputName + LeavesConfig.modify.fakeplayer.suffix;
    }

    public static boolean isCreateLegal(@NotNull String name) {
        if (!name.matches("^[a-zA-Z0-9_]{4,16}$")) {
            return false;
        }

        if (Bukkit.getPlayerExact(name) != null || BotList.INSTANCE.getBotByName(name) != null) {
            return false;
        }

        if (LeavesConfig.modify.fakeplayer.unableNames.contains(name)) {
            return false;
        }

        return BotList.INSTANCE.bots.size() < LeavesConfig.modify.fakeplayer.limit;
    }
}
