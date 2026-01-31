package org.leavesmc.leaves.bot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BotInventoryMenu extends ChestMenu {

    private final BotInventoryContainer botContainer;

    public BotInventoryMenu(int syncId, Inventory playerInventory, BotInventoryContainer botContainer) {
        super(MenuType.GENERIC_9x6, syncId, playerInventory, botContainer, 6);
        this.botContainer = botContainer;
    }

    @Override
    public void clicked(int slotIndex, int button, @NotNull ClickType actionType, @NotNull Player player) {
        if (slotIndex >= 0 && slotIndex < 54) {
            int realSlot = botContainer.convertSlot(slotIndex);
            if (realSlot == -999) {
                // Button click - trigger action and prevent item movement
                botContainer.triggerAction(slotIndex);
                this.sendAllDataToRemote();
                return;
            }
        }
        super.clicked(slotIndex, button, actionType, player);
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (index >= 0 && index < 54) {
            int realSlot = botContainer.convertSlot(index);
            if (realSlot == -999) {
                return ItemStack.EMPTY;
            }
        }
        return super.quickMoveStack(player, index);
    }
}
