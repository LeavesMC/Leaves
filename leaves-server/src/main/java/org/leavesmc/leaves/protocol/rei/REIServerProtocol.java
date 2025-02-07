package org.leavesmc.leaves.protocol.rei;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.LeavesProtocolManager.EmptyPayload;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemGrabPayload;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemHotbarPayload;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemMessagePayload;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemPayload;

@LeavesProtocol(namespace = "roughlyenoughitems")
public class REIServerProtocol {

    public static final String PROTOCOL_ID = "roughlyenoughitems";

    @Contract("_ -> new")
    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID, path);
    }

    @ProtocolHandler.PayloadReceiver(payload = EmptyPayload.class, payloadId = "delete_item")
    public static void handleDeleteItem(ServerPlayer player, EmptyPayload payload) {
        if (!check(player, true)) {
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
        if (!check(player, true)) {
            return;
        }

        ItemStack stack = payload.item();
        if (player.getInventory().add(stack.copy())) {
            ProtocolUtils.sendPayloadPacket(player, new CreateItemMessagePayload(stack.copy(), player.getScoreboardName()));
        } else {
            player.displayClientMessage(Component.translatable("text.rei.failed_cheat_items"), false);
        }
    }

    @ProtocolHandler.PayloadReceiver(payload = CreateItemGrabPayload.class, payloadId = "create_item_grab")
    public static void handleCreateItemGrab(ServerPlayer player, CreateItemGrabPayload payload) {
        if (!check(player, true)) {
            return;
        }

        AbstractContainerMenu menu = player.containerMenu;
        ItemStack itemStack = payload.item();
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
        if (!check(player, true)) {
            return;
        }

        ItemStack stack = payload.item();
        int hotbarSlotId = payload.hotbarSlot();
        if (hotbarSlotId >= 0 && hotbarSlotId < 9) {
            AbstractContainerMenu menu = player.containerMenu;
            player.getInventory().items.set(hotbarSlotId, stack.copy());
            menu.broadcastChanges();
            ProtocolUtils.sendPayloadPacket(player, new CreateItemMessagePayload(stack, player.getScoreboardName()));
        } else {
            player.displayClientMessage(Component.translatable("text.rei.failed_cheat_items"), false);
        }
    }

    private static boolean check(ServerPlayer player, boolean needOP) {
        if (!LeavesConfig.protocol.reiServerProtocol) {
            return false;
        }

        if (needOP && MinecraftServer.getServer().getPlayerList().isOp(player.gameProfile)) { // TODO check permission node
            player.displayClientMessage(Component.translatable("text.rei.no_permission_cheat").withStyle(ChatFormatting.RED), false);
            return false;
        }
        return true;
    }
}
