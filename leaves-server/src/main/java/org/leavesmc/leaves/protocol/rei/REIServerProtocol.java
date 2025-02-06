package org.leavesmc.leaves.protocol.rei;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemGrabPayload;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemHotbarPayload;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemMessagePayload;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemPayload;
import org.leavesmc.leaves.protocol.rei.payload.DeleteItemPayload;

@LeavesProtocol(namespace = "rei")
public class REIServerProtocol {

    @ProtocolHandler.PayloadReceiver(payload = DeleteItemPayload.class, payloadId = "delete_item")
    public static void handleDeleteItem(ServerPlayer player, DeleteItemPayload payload) {
        if (!LeavesConfig.protocol.reiServerProtocol) {
            return;
        }
        if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
            player.displayClientMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (!menu.getCarried().isEmpty()) {
            menu.setCarried(ItemStack.EMPTY);
            menu.broadcastChanges();
        }
    }

    @ProtocolHandler.PayloadReceiver(payload = CreateItemPayload.class, payloadId = "create_item")
    public static void handleCreateItem(ServerPlayer player, CreateItemPayload payload) {
        if (!LeavesConfig.protocol.reiServerProtocol) {
            return;
        }
        if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
            player.displayClientMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
            return;
        }
        ItemStack stack = payload.getItem();
        if (player.getInventory().add(stack.copy())) {
            ProtocolUtils.sendPayloadPacket(player, new CreateItemMessagePayload(stack.copy(), player.getScoreboardName()));
        } else {
            player.displayClientMessage(Component.translatable("text.rei.failed_cheat_items"), false);
        }
    }

    @ProtocolHandler.PayloadReceiver(payload = CreateItemGrabPayload.class, payloadId = "create_item_grab")
    public static void handleCreateItemGrab(ServerPlayer player, CreateItemGrabPayload payload) {
        if (!LeavesConfig.protocol.reiServerProtocol) {
            return;
        }
        if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
            player.displayClientMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        ItemStack itemStack = payload.getItem();
        ItemStack stack = itemStack.copy();
        if (!menu.getCarried().isEmpty() && ItemStack.isSameItemSameComponents(menu.getCarried(), stack)) {
            stack.setCount(Mth.clamp(stack.getCount() + menu.getCarried().getCount(), 1, stack.getMaxStackSize()));
        } else if (!menu.getCarried().isEmpty()) {
            return;
        }
        menu.setCarried(stack.copy());
        menu.broadcastChanges();
        ProtocolUtils.sendPayloadPacket(player, new CreateItemMessagePayload(stack, player.getScoreboardName()));
    }

    @ProtocolHandler.PayloadReceiver(payload = CreateItemHotbarPayload.class, payloadId = "create_item_hotbar")
    public static void handleCreateItemHotbar(ServerPlayer player, CreateItemHotbarPayload payload) {
        if (!LeavesConfig.protocol.reiServerProtocol) {
            return;
        }
        if (player.getServer().getProfilePermissions(player.getGameProfile()) < player.getServer().getOperatorUserPermissionLevel()) {
            player.displayClientMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
            return;
        }
        ItemStack stack = payload.getItem();
        int hotbarSlotId = payload.getHotbarSlot();
        if (hotbarSlotId >= 0 && hotbarSlotId < 9) {
            AbstractContainerMenu menu = player.containerMenu;
            player.getInventory().items.set(hotbarSlotId, stack.copy());
            menu.broadcastChanges();
            ProtocolUtils.sendPayloadPacket(player, new CreateItemMessagePayload(stack, player.getScoreboardName()));
        } else {
            player.displayClientMessage(Component.translatable("text.rei.failed_cheat_items"), false);
        }
    }
}
