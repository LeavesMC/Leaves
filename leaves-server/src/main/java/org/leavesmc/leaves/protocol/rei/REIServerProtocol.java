package org.leavesmc.leaves.protocol.rei;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemGrabPayload;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemHotbarPayload;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemMessagePayload;
import org.leavesmc.leaves.protocol.rei.payload.CreateItemPayload;
import org.leavesmc.leaves.protocol.rei.payload.DeleteItemPayload;

import java.util.ArrayList;

@LeavesProtocol(namespace = "rei")
public class REIServerProtocol {
    public static final ResourceLocation MOVE_ITEMS_PACKET = ResourceLocation.fromNamespaceAndPath("roughlyenoughitems", "move_items");
    public static final ResourceLocation MOVE_ITEMS_NEW_PACKET = ResourceLocation.fromNamespaceAndPath("roughlyenoughitems", "move_items_new");
    public static final ResourceLocation NOT_ENOUGH_ITEMS_PACKET = ResourceLocation.fromNamespaceAndPath("roughlyenoughitems", "og_not_enough");

    @ProtocolHandler.PayloadReceiver(payload = DeleteItemPayload.class, payloadId = "delete_item")
    public static void handleDeleteItem(ServerPlayer player, DeleteItemPayload payload) {
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

    /*
    public static void onInitialize() {

        NetworkManager.registerReceiver(NetworkManager.c2s(), MOVE_ITEMS_PACKET, Collections.singletonList(new SplitPacketTransformer()), (packetByteBuf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            CategoryIdentifier<Display> category = CategoryIdentifier.of(packetByteBuf.readResourceLocation());
            AbstractContainerMenu container = player.containerMenu;
            InventoryMenu playerContainer = player.inventoryMenu;
            try {
                boolean shift = packetByteBuf.readBoolean();
                try {
                    LegacyInputSlotCrafter<AbstractContainerMenu, Container, Display> crafter = LegacyInputSlotCrafter.start(category, container, player, packetByteBuf.readNbt(), shift);
                } catch (InputSlotCrafter.NotEnoughMaterialsException e) {
                    if (!(container instanceof RecipeBookMenu)) {
                        return;
                    }
                } catch (IllegalStateException e) {
                    player.sendSystemMessage(Component.translatable(e.getMessage()).withStyle(ChatFormatting.RED));
                } catch (Exception e) {
                    player.sendSystemMessage(Component.translatable("error.rei.internal.error", e.getMessage()).withStyle(ChatFormatting.RED));
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        NetworkManager.registerReceiver(NetworkManager.c2s(), MOVE_ITEMS_NEW_PACKET, Collections.singletonList(new SplitPacketTransformer()), (packetByteBuf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            CategoryIdentifier<Display> category = CategoryIdentifier.of(packetByteBuf.readResourceLocation());
            AbstractContainerMenu container = player.containerMenu;
            InventoryMenu playerContainer = player.inventoryMenu;
            try {
                boolean shift = packetByteBuf.readBoolean();
                try {
                    CompoundTag nbt = packetByteBuf.readNbt();
                    int version = nbt.getInt("Version");
                    if (version != 1) throw new IllegalStateException("Server and client REI protocol version mismatch! Server: 1, Client: " + version);
                    List<InputIngredient<ItemStack>> inputs = readInputs(nbt.getList("Inputs", Tag.TAG_COMPOUND));
                    List<SlotAccessor> input = readSlots(container, player, nbt.getList("InputSlots", Tag.TAG_COMPOUND));
                    List<SlotAccessor> inventory = readSlots(container, player, nbt.getList("InventorySlots", Tag.TAG_COMPOUND));
                    NewInputSlotCrafter<AbstractContainerMenu, Container> crafter = new NewInputSlotCrafter<>(container, input, inventory, inputs);
                    crafter.fillInputSlots(player, shift);
                } catch (InputSlotCrafter.NotEnoughMaterialsException e) {
                    if (!(container instanceof RecipeBookMenu)) {
                        return;
                    }
                } catch (IllegalStateException e) {
                    player.sendSystemMessage(Component.translatable(e.getMessage()).withStyle(ChatFormatting.RED));
                } catch (Exception e) {
                    player.sendSystemMessage(Component.translatable("error.rei.internal.error", e.getMessage()).withStyle(ChatFormatting.RED));
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static List<SlotAccessor> readSlots(AbstractContainerMenu menu, Player player, ListTag tag) {
        List<SlotAccessor> slots = new ArrayList<>();
        for (Tag t : tag) {
            slots.add(SlotAccessorRegistry.getInstance().read(menu, player, (CompoundTag) t));
        }
        return slots;
    }

    private static List<InputIngredient<ItemStack>> readInputs(ListTag tag) {
        List<InputIngredient<ItemStack>> inputs = new ArrayList<>();
        for (Tag t : tag) {
            CompoundTag compoundTag = (CompoundTag) t;
            InputIngredient<EntryStack<?>> stacks = InputIngredient.of(compoundTag.getInt("Index"), EntryIngredient.read(compoundTag.getList("Ingredient", Tag.TAG_COMPOUND)));
            inputs.add(InputIngredient.withType(stacks, VanillaEntryTypes.ITEM));
        }
        return inputs;
    }

     */
}
