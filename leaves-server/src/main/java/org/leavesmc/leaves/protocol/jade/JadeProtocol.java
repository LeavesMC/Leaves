package org.leavesmc.leaves.protocol.jade;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.leavesmc.leaves.protocol.jade.accessor.BlockAccessor;
import org.leavesmc.leaves.protocol.jade.accessor.BlockAccessorImpl;
import org.leavesmc.leaves.protocol.jade.accessor.DataAccessor;
import org.leavesmc.leaves.protocol.jade.accessor.EntityAccessor;
import org.leavesmc.leaves.protocol.jade.accessor.EntityAccessorImpl;
import org.leavesmc.leaves.protocol.jade.payload.ClientHandshakePayload;
import org.leavesmc.leaves.protocol.jade.payload.ReceiveDataPayload;
import org.leavesmc.leaves.protocol.jade.payload.RequestBlockPayload;
import org.leavesmc.leaves.protocol.jade.payload.RequestEntityPayload;
import org.leavesmc.leaves.protocol.jade.payload.ServerHandshakePayload;
import org.leavesmc.leaves.protocol.jade.provider.IJadeProvider;
import org.leavesmc.leaves.protocol.jade.provider.IServerDataProvider;
import org.leavesmc.leaves.protocol.jade.provider.IServerExtensionProvider;
import org.leavesmc.leaves.protocol.jade.provider.ItemStorageExtensionProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.BeehiveProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.BrewingStandProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.CampfireProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.ChiseledBookshelfProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.CommandBlockProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.FurnaceProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.HopperLockProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.ItemStorageProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.JukeboxProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.LecternProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.MobSpawnerCooldownProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.ObjectNameProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.RedstoneProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.AnimalOwnerProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.MobBreedingProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.MobGrowthProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.NextEntityDropProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.StatusEffectsProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.ZombieVillagerProvider;
import org.leavesmc.leaves.protocol.jade.util.HierarchyLookup;
import org.leavesmc.leaves.protocol.jade.util.LootTableMineableCollector;
import org.leavesmc.leaves.protocol.jade.util.PairHierarchyLookup;
import org.leavesmc.leaves.protocol.jade.util.PriorityStore;
import org.leavesmc.leaves.protocol.jade.util.WrappedHierarchyLookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@LeavesProtocol(namespace = "jade")
public class JadeProtocol {

    public static PriorityStore<ResourceLocation, IJadeProvider> priorities;
    private static Set<UUID> enabledPlayers = new HashSet<>();
    private static List<Block> shearableBlocks = null;

    public static final String PROTOCOL_ID = "jade";
    public static final String PROTOCOL_VERSION = "7";

    public static final HierarchyLookup<IServerDataProvider<EntityAccessor>> entityDataProviders = new HierarchyLookup<>(Entity.class);
    public static final PairHierarchyLookup<IServerDataProvider<BlockAccessor>> blockDataProviders = new PairHierarchyLookup<>(new HierarchyLookup<>(Block.class), new HierarchyLookup<>(BlockEntity.class));
    public static final WrappedHierarchyLookup<IServerExtensionProvider<ItemStack>> itemStorageProviders = new WrappedHierarchyLookup<>();

    public static final StreamCodec<ByteBuf, Object> PRIMITIVE_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull Object decode(ByteBuf buf) {
            byte b = buf.readByte();
            if (b == 0) {
                return false;
            } else if (b == 1) {
                return true;
            } else if (b == 2) {
                return ByteBufCodecs.VAR_INT.decode(buf);
            } else if (b == 3) {
                return ByteBufCodecs.FLOAT.decode(buf);
            } else if (b == 4) {
                return ByteBufCodecs.STRING_UTF8.decode(buf);
            } else if (b > 20) {
                return b - 20;
            }
            throw new IllegalArgumentException("Unknown primitive type: " + b);
        }

        @Override
        public void encode(ByteBuf buf, Object o) {
            switch (o) {
                case Boolean b -> buf.writeByte(b ? 1 : 0);
                case Number n -> {
                    float f = n.floatValue();
                    if (f != (int) f) {
                        buf.writeByte(3);
                        ByteBufCodecs.FLOAT.encode(buf, f);
                    }
                    int i = n.intValue();
                    if (i <= Byte.MAX_VALUE - 20 && i >= 0) {
                        buf.writeByte(i + 20);
                    } else {
                        ByteBufCodecs.VAR_INT.encode(buf, i);
                    }
                }
                case String s -> {
                    buf.writeByte(4);
                    ByteBufCodecs.STRING_UTF8.encode(buf, s);
                }
                case Enum<?> anEnum -> {
                    buf.writeByte(4);
                    ByteBufCodecs.STRING_UTF8.encode(buf, anEnum.name());
                }
                case null -> throw new NullPointerException();
                default -> throw new IllegalArgumentException("Unknown primitive type: %s (%s)".formatted(o, o.getClass()));
            }
        }
    };

    @Contract("_ -> new")
    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(PROTOCOL_ID, path);
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation mc_id(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    private static boolean isPrimaryKey(ResourceLocation key) {
        return !key.getPath().contains(".");
    }

    private static ResourceLocation getPrimaryKey(ResourceLocation key) {
        return ResourceLocation.tryBuild(key.getNamespace(), key.getPath().substring(0, key.getPath().indexOf('.')));
    }

    @ProtocolHandler.Init
    public static void init() {
        priorities = new PriorityStore<>(IJadeProvider::getDefaultPriority, IJadeProvider::getUid);
        priorities.setSortingFunction((store, allKeys) -> {
            List<ResourceLocation> keys = allKeys.stream()
                    .filter(JadeProtocol::isPrimaryKey)
                    .sorted(Comparator.comparingInt(store::byKey))
                    .collect(Collectors.toCollection(ArrayList::new));
            allKeys.stream().filter(Predicate.not(JadeProtocol::isPrimaryKey)).forEach($ -> {
                int index = keys.indexOf(JadeProtocol.getPrimaryKey($));
                keys.add(index + 1, $);
            });
            return keys;
        });

        // core plugin
        blockDataProviders.register(BlockEntity.class, ObjectNameProvider.ForBlock.INSTANCE);

        // universal plugin
        entityDataProviders.register(Entity.class, ItemStorageProvider.getEntity());
        blockDataProviders.register(Block.class, ItemStorageProvider.getBlock());

        itemStorageProviders.register(Object.class, ItemStorageExtensionProvider.INSTANCE);
        itemStorageProviders.register(Block.class, ItemStorageExtensionProvider.INSTANCE);

        // vanilla plugin
        entityDataProviders.register(Entity.class, AnimalOwnerProvider.INSTANCE);
        entityDataProviders.register(LivingEntity.class, StatusEffectsProvider.INSTANCE);
        entityDataProviders.register(AgeableMob.class, MobGrowthProvider.INSTANCE);
        entityDataProviders.register(Tadpole.class, MobGrowthProvider.INSTANCE);
        entityDataProviders.register(Animal.class, MobBreedingProvider.INSTANCE);
        entityDataProviders.register(Allay.class, MobBreedingProvider.INSTANCE);

        entityDataProviders.register(Chicken.class, NextEntityDropProvider.INSTANCE);
        entityDataProviders.register(Armadillo.class, NextEntityDropProvider.INSTANCE);

        entityDataProviders.register(ZombieVillager.class, ZombieVillagerProvider.INSTANCE);

        blockDataProviders.register(BrewingStandBlockEntity.class, BrewingStandProvider.INSTANCE);
        blockDataProviders.register(BeehiveBlockEntity.class, BeehiveProvider.INSTANCE);
        blockDataProviders.register(CommandBlockEntity.class, CommandBlockProvider.INSTANCE);
        blockDataProviders.register(JukeboxBlockEntity.class, JukeboxProvider.INSTANCE);
        blockDataProviders.register(LecternBlockEntity.class, LecternProvider.INSTANCE);

        blockDataProviders.register(ComparatorBlockEntity.class, RedstoneProvider.INSTANCE);
        blockDataProviders.register(HopperBlockEntity.class, HopperLockProvider.INSTANCE);
        blockDataProviders.register(CalibratedSculkSensorBlockEntity.class, RedstoneProvider.INSTANCE);

        blockDataProviders.register(AbstractFurnaceBlockEntity.class, FurnaceProvider.INSTANCE);
        blockDataProviders.register(ChiseledBookShelfBlockEntity.class, ChiseledBookshelfProvider.INSTANCE);
        blockDataProviders.register(TrialSpawnerBlockEntity.class, MobSpawnerCooldownProvider.INSTANCE);

        blockDataProviders.idMapped();
        entityDataProviders.idMapped();
        itemStorageProviders.register(CampfireBlock.class, CampfireProvider.INSTANCE);

        blockDataProviders.loadComplete(priorities);
        entityDataProviders.loadComplete(priorities);
        itemStorageProviders.loadComplete(priorities);

        try {
            shearableBlocks = Collections.unmodifiableList(LootTableMineableCollector.execute(
                    MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.LOOT_TABLE),
                    Items.SHEARS.getDefaultInstance()));
        } catch (Throwable ignore) {
            shearableBlocks = List.of();
            LeavesLogger.LOGGER.severe("Failed to collect shearable blocks");
        }
    }

    @ProtocolHandler.PlayerLeave
    public static void onPlayerQuit(ServerPlayer player) {
        enabledPlayers.remove(player.getUUID());
    }

    @ProtocolHandler.PayloadReceiver(payload = ClientHandshakePayload.class, payloadId = "client_handshake")
    public static void clientHandshake(ServerPlayer player, ClientHandshakePayload payload) {
        if (!LeavesConfig.protocol.jadeProtocol) {
            return;
        }
        if (!payload.protocolVersion().equals(PROTOCOL_VERSION)) {
            player.sendSystemMessage(Component.literal("You are using a different version of Jade than the server. Please update Jade or report to the server operator").withColor(0xff0000));
            return;
        }
        ProtocolUtils.sendPayloadPacket(player, new ServerHandshakePayload(Collections.emptyMap(), shearableBlocks, blockDataProviders.mappedIds(), entityDataProviders.mappedIds()));
        enabledPlayers.add(player.getUUID());
    }

    @ProtocolHandler.PayloadReceiver(payload = RequestEntityPayload.class, payloadId = "request_entity")
    public static void requestEntityData(ServerPlayer player, RequestEntityPayload payload) {
        if (!LeavesConfig.protocol.jadeProtocol) {
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        server.execute(() -> {
            Level world = player.level();
            boolean showDetails = payload.data().showDetails();
            Entity entity = world.getEntity(payload.data().id());
            double maxDistance = Mth.square(player.entityInteractionRange() + 21);

            if (entity == null || player.distanceToSqr(entity) > maxDistance) {
                return;
            }

            if (payload.data().partIndex() >= 0 && entity instanceof EnderDragon dragon) {
                EnderDragonPart[] parts = dragon.getSubEntities();
                if (payload.data().partIndex() < parts.length) {
                    entity = parts[payload.data().partIndex()];
                }
            }

            var providers = entityDataProviders.get(entity);
            if (providers.isEmpty()) {
                return;
            }


            final Entity finalEntity = entity;
            DataAccessor tag = new DataAccessor(world);
            EntityAccessor accessor = new EntityAccessorImpl.Builder()
                    .level(world)
                    .player(player)
                    .showDetails(showDetails)
                    .entity(entity)
                    .hit(Suppliers.memoize(() -> new EntityHitResult(finalEntity, payload.data().hitVec())))
                    .build();

            for (IServerDataProvider<EntityAccessor> provider : providers) {
                try {
                    provider.appendServerData(tag, accessor);
                } catch (Exception e) {
                    LeavesLogger.LOGGER.warning("Error while saving data for entity " + entity);
                }
            }
            tag.putInt("EntityId", entity.getId());

            ProtocolUtils.sendPayloadPacket(player, new ReceiveDataPayload(tag));
        });
    }

    @ProtocolHandler.PayloadReceiver(payload = RequestBlockPayload.class, payloadId = "request_block")
    public static void requestBlockData(ServerPlayer player, RequestBlockPayload payload) {
        if (!LeavesConfig.protocol.jadeProtocol) {
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        server.execute(() -> {
            Level world = player.level();
            BlockState blockState = payload.data().blockState();
            Block block = blockState.getBlock();
            BlockHitResult result = payload.data().hit();
            BlockPos pos = result.getBlockPos();
            boolean showDetails = payload.data().showDetails();

            double maxDistance = Mth.square(player.blockInteractionRange() + 21);
            if (pos.distSqr(player.blockPosition()) > maxDistance || !world.isLoaded(pos)) {
                return;
            }

            BlockEntity blockEntity = null;
            if (blockState.hasBlockEntity()) {
                blockEntity = world.getBlockEntity(pos);
            }

            List<IServerDataProvider<BlockAccessor>> providers;
            if (blockEntity != null) {
                providers = blockDataProviders.getMerged(block, blockEntity);
            } else {
                providers = blockDataProviders.first.get(block);
            }

            if (providers.isEmpty()) {
                player.getBukkitEntity().sendMessage("Provider is empty!");
                return;
            }

            DataAccessor tag = new DataAccessor(world);
            BlockAccessor accessor = new BlockAccessorImpl.Builder()
                    .level(world)
                    .player(player)
                    .showDetails(showDetails)
                    .hit(result)
                    .blockState(blockState)
                    .blockEntity(blockEntity)
                    .fakeBlock(payload.data().fakeBlock())
                    .build();

            for (IServerDataProvider<BlockAccessor> provider : providers) {
                try {
                    provider.appendServerData(tag, accessor);
                } catch (Exception e) {
                    LeavesLogger.LOGGER.warning("Error while saving data for block " + blockState);
                }
            }
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            tag.putString("BlockId", BuiltInRegistries.BLOCK.getKey(block).toString());
            ProtocolUtils.sendPayloadPacket(player, new ReceiveDataPayload(tag));
        });
    }

    @ProtocolHandler.ReloadServer
    public static void onServerReload() {
        if (LeavesConfig.protocol.jadeProtocol) {
            enableAllPlayer();
        }
    }

    public static void enableAllPlayer() {
        for (ServerPlayer player : MinecraftServer.getServer().getPlayerList().players) {
            if (enabledPlayers.contains(player.getUUID())) {
                ProtocolUtils.sendPayloadPacket(player, new ServerHandshakePayload(Collections.emptyMap(), shearableBlocks, blockDataProviders.mappedIds(), entityDataProviders.mappedIds()));
            }
        }
    }
}