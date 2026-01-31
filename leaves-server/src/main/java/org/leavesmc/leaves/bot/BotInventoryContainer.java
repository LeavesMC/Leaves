package org.leavesmc.leaves.bot;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BotInventoryContainer extends Inventory {

    private static final Map<Integer, ItemStack> commonButtons = new HashMap<>();
    private static final Map<Integer, ItemStack> hotbarButtons = new HashMap<>();
    private static final Map<Integer, ItemStack> actionButtons = new HashMap<>();

    static {
        commonButtons.put(0, createButton(Items.REDSTONE_BLOCK, Component.literal("停止所有动作").withStyle(ChatFormatting.RED)));
        commonButtons.put(5, createButton(Items.GUNPOWDER, Component.literal("丢弃手持物品").withStyle(ChatFormatting.GOLD)));

        for (int i = 0; i < 9; i++) {
            hotbarButtons.put(i + 9, createButton(Items.LIGHT_GRAY_STAINED_GLASS_PANE, Component.literal("切换至快捷栏第 " + (i + 1) + " 格").withStyle(ChatFormatting.WHITE)));
        }

        actionButtons.put(9, createButton(Items.IRON_SWORD, Component.literal("攻击一次").withStyle(ChatFormatting.YELLOW)));
        actionButtons.put(10, createButton(Items.GOLDEN_SWORD, Component.literal("连续攻击").withStyle(ChatFormatting.YELLOW)));
        actionButtons.put(11, createButton(Items.DIAMOND_SWORD, Component.literal("间歇攻击 (1.2s)").withStyle(ChatFormatting.YELLOW)));
        actionButtons.put(12, createButton(Items.IRON_HOE, Component.literal("使用一次").withStyle(ChatFormatting.AQUA)));
        actionButtons.put(13, createButton(Items.GOLDEN_HOE, Component.literal("连续使用").withStyle(ChatFormatting.AQUA)));
        actionButtons.put(14, createButton(Items.DIAMOND_HOE, Component.literal("间歇使用 (1.2s)").withStyle(ChatFormatting.AQUA)));
        actionButtons.put(15, createButton(Items.FEATHER, Component.literal("跳跃一次").withStyle(ChatFormatting.GREEN)));
        actionButtons.put(16, createButton(Items.BLAZE_ROD, Component.literal("连续跳跃").withStyle(ChatFormatting.GREEN)));
        actionButtons.put(17, createButton(Items.LEATHER_BOOTS, Component.literal("切换潜行").withStyle(ChatFormatting.GRAY)));
    }

    private static ItemStack createButton(Item item, Component name) {
        CompoundTag customData = new CompoundTag();
        customData.putBoolean("Leaves.Gui.Placeholder", true);

        DataComponentPatch patch = DataComponentPatch.builder()
            .set(DataComponents.CUSTOM_NAME, Component.empty().append(name).withStyle(style -> style.withItalic(false)))
            .set(DataComponents.CUSTOM_DATA, CustomData.of(customData))
            .build();

        ItemStack stack = new ItemStack(item);
        stack.applyComponents(patch);
        return stack;
    }

    private static final ItemStack emptyButton = createButton(Items.STRUCTURE_VOID, Component.empty());

    private final Inventory original;
    private int buttonPage = 0; // 0: Hotbar, 1: Actions

    public BotInventoryContainer(Inventory original) {
        super(original.player, original.equipment);
        this.original = original;
    }

    @Override
    public int getContainerSize() {
        return 54;
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty();
    }

    @Override
    @NotNull
    public ItemStack getItem(int slot) {
        int realSlot = convertSlot(slot);
        if (realSlot == -999) {
            if (slot == 8) {
                return buttonPage == 0 ? 
                    createButton(Items.CHEST, Component.literal("当前状态: 切换快捷栏 (点击切换到动作页)").withStyle(ChatFormatting.GREEN)) :
                    createButton(Items.RECOVERY_COMPASS, Component.literal("当前状态: 触发动作 (点击切换到快捷栏页)").withStyle(ChatFormatting.BLUE));
            }
            if (commonButtons.containsKey(slot)) {
                return commonButtons.get(slot);
            }
            if (buttonPage == 0) {
                if (slot >= 9 && slot <= 17) {
                    int hotbarSlot = slot - 9;
                    ItemStack botItem = original.getItem(hotbarSlot);
                    ItemStack stack = botItem.isEmpty() ? new ItemStack(Items.STRUCTURE_VOID) : botItem.copy();
                    Component name = Component.literal("切换至快捷栏第 " + (hotbarSlot + 1) + " 格")
                        .withStyle(hotbarSlot == original.selected ? ChatFormatting.YELLOW : ChatFormatting.WHITE);
                    if (hotbarSlot == original.selected) {
                        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    }
                    
                    DataComponentPatch patch = DataComponentPatch.builder()
                        .set(DataComponents.CUSTOM_NAME, Component.empty().append(name).withStyle(style -> style.withItalic(false)))
                        .build();
                    stack.applyComponents(patch);
                    return stack;
                }
                return hotbarButtons.getOrDefault(slot, emptyButton);
            } else {
                return actionButtons.getOrDefault(slot, emptyButton);
            }
        }
        return original.getItem(realSlot);
    }

    public int convertSlot(int slot) {
        if (!(player instanceof ServerBot bot)) return -999;
        return switch (slot) {
            // Current Mainhand
            case 6 -> bot.getInventory().selected;

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
    @NotNull
    public ItemStack removeItem(int slot, int amount) {
        int realSlot = convertSlot(slot);
        if (realSlot == -999) {
            triggerAction(slot);
            return ItemStack.EMPTY;
        }
        ItemStack removed = original.removeItem(realSlot, amount);
        player.detectEquipmentUpdates();
        return removed;
    }

    @Override
    @NotNull
    public ItemStack removeItemNoUpdate(int slot) {
        int realSlot = convertSlot(slot);
        if (realSlot == -999) {
            triggerAction(slot);
            return ItemStack.EMPTY;
        }
        return original.removeItemNoUpdate(realSlot);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        int realSlot = convertSlot(slot);
        if (realSlot == -999) {
            triggerAction(slot);
            return;
        }
        original.setItem(realSlot, stack);
        player.detectEquipmentUpdates();
    }

    public void triggerAction(int slot) {
        if (!(player instanceof ServerBot bot)) {
            return;
        }

        // Add logging for debugging
        Bukkit.getLogger().log(Level.INFO, "BotInventory interaction: slot " + slot + ", page " + buttonPage);

        switch (slot) {
            case 0 -> {
                bot.stopAllActions();
                bot.setShiftKeyDown(false);
                bot.setSprinting(false);
            }
            case 5 -> {
                ItemStack stack = bot.getMainHandItem();
                if (!stack.isEmpty()) {
                    ItemStack toDrop = stack.copy();
                    toDrop.setCount(1);
                    if (bot.drop(toDrop, false, true) != null) {
                        stack.shrink(1);
                    }
                }
            }
            case 8 -> {
                buttonPage = (buttonPage + 1) % 2;
                Bukkit.getLogger().log(Level.INFO, "Toggled buttonPage to " + buttonPage);
            }
            default -> {
                if (slot >= 9 && slot <= 17) {
                    if (buttonPage == 0) {
                        bot.getInventory().selected = slot - 9;
                        bot.getBukkitEntity().getInventory().setHeldItemSlot(slot - 9);
                        bot.detectEquipmentUpdates();
                    } else {
                        switch (slot) {
                            case 9 -> bot.addBotAction(createTimerAction(new ServerAttackAction(), 1, 1), null);
                            case 10 -> bot.addBotAction(createTimerAction(new ServerAttackAction(), 1, -1), null);
                            case 11 -> bot.addBotAction(createTimerAction(new ServerAttackAction(), 24, -1), null);
                            case 12 -> bot.addBotAction(createTimerAction(new ServerUseItemAutoAction(), 1, 1), null);
                            case 13 -> bot.addBotAction(createTimerAction(new ServerUseItemAutoAction(), 1, -1), null);
                            case 14 -> bot.addBotAction(createTimerAction(new ServerUseItemAutoAction(), 24, -1), null);
                            case 15 -> bot.addBotAction(createTimerAction(new ServerJumpAction(), 1, 1), null);
                            case 16 -> bot.addBotAction(createTimerAction(new ServerJumpAction(), 1, -1), null);
                            case 17 -> {
                                if (bot.isShiftKeyDown()) {
                                    bot.setShiftKeyDown(false);
                                    for (int i = 0; i < bot.getBotActions().size(); i++) {
                                        if ("sneak".equals(bot.getBotActions().get(i).getName())) {
                                            bot.getBotActions().get(i).stop(bot, BotActionStopEvent.Reason.PLUGIN);
                                            break;
                                        }
                                    }
                                } else {
                                    bot.addBotAction(new ServerSneakAction(), null);
                                }
                            }
                        }
                    }
                }
            }
        }
        refreshViewers(bot);
    }

    private void refreshViewers(ServerBot bot) {
        for (ServerPlayer viewer : MinecraftServer.getServer().getPlayerList().getPlayers()) {
            if (viewer.containerMenu instanceof ChestMenu menu) {
                if (!menu.slots.isEmpty() && menu.getSlot(0).container == this) {
                    menu.sendAllDataToRemote();
                }
            }
        }
    }

    private <T extends AbstractTimerBotAction<T>> T createTimerAction(T action, int interval, int count) {
        action.setDoIntervalTick(interval);
        action.setDoNumber(count);
        return action;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public void clearContent() {
        original.clearContent();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.player.isRemoved()) {
            return false;
        }
        return !(player.distanceToSqr(this.player) > 64.0);
    }
}