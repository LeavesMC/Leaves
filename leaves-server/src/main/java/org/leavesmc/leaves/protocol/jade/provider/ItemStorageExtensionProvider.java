package org.leavesmc.leaves.protocol.jade.provider;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.LockCode;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;
import org.leavesmc.leaves.protocol.jade.accessor.Accessor;
import org.leavesmc.leaves.protocol.jade.accessor.BlockAccessor;
import org.leavesmc.leaves.protocol.jade.util.ItemCollector;
import org.leavesmc.leaves.protocol.jade.util.ItemIterator;
import org.leavesmc.leaves.protocol.jade.util.ViewGroup;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public enum ItemStorageExtensionProvider implements IServerExtensionProvider<ItemStack> {
    INSTANCE;

    public static final Cache<Object, ItemCollector<?>> targetCache = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(60, TimeUnit.SECONDS).build();

    private static final ResourceLocation UNIVERSAL_ITEM_STORAGE = JadeProtocol.mc_id("item_storage.default");

    @Override
    public List<ViewGroup<ItemStack>> getGroups(Accessor<?> request) {
        Object target = request.getTarget();

        switch (target) {
            case null -> {
                return createItemCollector(request).update(request);
            }
            case RandomizableContainer te when te.getLootTable() != null -> {
                return List.of();
            }
            case ContainerEntity containerEntity when containerEntity.getContainerLootTable() != null -> {
                return List.of();
            }
            default -> {
            }
        }

        Player player = request.getPlayer();
        if (!player.isCreative() && !player.isSpectator() && target instanceof BaseContainerBlockEntity te) {
            if (te.lockKey != LockCode.NO_LOCK) {
                return List.of();
            }
        }

        if (target instanceof EnderChestBlockEntity) {
            PlayerEnderChestContainer inventory = player.getEnderChestInventory();
            return new ItemCollector<>(new ItemIterator.ContainerItemIterator(x -> inventory, 0)).update(request);
        }

        ItemCollector<?> itemCollector;
        try {
            itemCollector = targetCache.get(target, () -> createItemCollector(request));
        } catch (ExecutionException e) {
            LeavesLogger.LOGGER.severe("Failed to get item collector for " + target);
            return null;
        }

        return itemCollector.update(request);
    }

    @Override
    public ResourceLocation getUid() {
        return UNIVERSAL_ITEM_STORAGE;
    }

    public static ItemCollector<?> createItemCollector(Accessor<?> request) {
        if (request.getTarget() instanceof AbstractHorse) {
            return new ItemCollector<>(new ItemIterator.ContainerItemIterator(o -> {
                if (o instanceof AbstractHorse horse) {
                    return horse.inventory;
                }
                return null;
            }, 2));
        }

        // TODO BlockEntity like fabric's ItemStorage

        final Container container = findContainer(request);
        if (container != null) {
            if (container instanceof ChestBlockEntity) {
                return new ItemCollector<>(new ItemIterator.ContainerItemIterator(o -> {
                    if (o instanceof ChestBlockEntity blockEntity) {
                        if (blockEntity.getBlockState().getBlock() instanceof ChestBlock chestBlock) {
                            Container compound = null;
                            if (blockEntity.getLevel() != null) {
                                compound = ChestBlock.getContainer(chestBlock, blockEntity.getBlockState(), blockEntity.getLevel(), blockEntity.getBlockPos(), false);
                            }
                            if (compound != null) {
                                return compound;
                            }
                        }
                        return blockEntity;
                    }
                    return null;
                }, 0));
            }
            return new ItemCollector<>(new ItemIterator.ContainerItemIterator(0));
        }

        return ItemCollector.EMPTY;
    }

    public static @Nullable Container findContainer(@NotNull Accessor<?> accessor) {
        Object target = accessor.getTarget();
        if (target == null && accessor instanceof BlockAccessor blockAccessor &&
            blockAccessor.getBlock() instanceof WorldlyContainerHolder holder) {
            return holder.getContainer(blockAccessor.getBlockState(), accessor.getLevel(), blockAccessor.getPosition());
        } else if (target instanceof Container container) {
            return container;
        }
        return null;
    }

    @Override
    public int getDefaultPriority() {
        return IServerExtensionProvider.super.getDefaultPriority() + 1000;
    }
}
