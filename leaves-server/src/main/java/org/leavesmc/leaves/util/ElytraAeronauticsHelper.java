package org.leavesmc.leaves.util;

import ca.spottedleaf.moonrise.common.PlatformHooks;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.plugin.MinecraftInternalPlugin;

import java.util.ArrayList;
import java.util.List;

public class ElytraAeronauticsHelper {

    public static final List<FireworkRocketEntity> proxyTickingFireworks = new ArrayList<>();
    public static ScheduledTask tickTask;
    private static boolean isActive;

    public static void setActive(boolean active) {
        isActive = active;
        if (!isActive) {
            if (tickTask != null) {
                tickTask.cancel();
            }
            tickTask = null;
        }
        tickTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(MinecraftInternalPlugin.INSTANCE, task -> {
            proxyTickingFireworks.removeIf(Entity::isRemoved);
            for (FireworkRocketEntity firework : proxyTickingFireworks) {
                firework.life++;
                Vec3 handHoldingItemAngle;
                if (firework.attachedToEntity == null || firework.life > firework.lifetime) {
                    firework.discard();
                    continue;
                }
                if (firework.attachedToEntity.isFallFlying()) {
                    if (firework.attachedToEntity instanceof ServerPlayer player) {
                        player.connection.send(new ClientboundSetEntityMotionPacket(player));
                    }
                    Vec3 lookAngle = firework.attachedToEntity.getLookAngle();
                    Vec3 deltaMovement = firework.attachedToEntity.getDeltaMovement();
                    firework.attachedToEntity.setDeltaMovement(deltaMovement.add(
                        lookAngle.x * 0.1 + (lookAngle.x * 1.5 - deltaMovement.x) * 0.5,
                        lookAngle.y * 0.1 + (lookAngle.y * 1.5 - deltaMovement.y) * 0.5,
                        lookAngle.z * 0.1 + (lookAngle.z * 1.5 - deltaMovement.z) * 0.5
                    ));
                    firework.attachedToEntity.hurtMarked = true;
                    handHoldingItemAngle = firework.attachedToEntity.getHandHoldingItemAngle(Items.FIREWORK_ROCKET);
                } else {
                    handHoldingItemAngle = Vec3.ZERO;
                }

                firework.setPos(
                    firework.attachedToEntity.getX() + handHoldingItemAngle.x,
                    firework.attachedToEntity.getY() + handHoldingItemAngle.y,
                    firework.attachedToEntity.getZ() + handHoldingItemAngle.z
                );
                firework.setDeltaMovement(firework.attachedToEntity.getDeltaMovement());
            }
        }, 1, 1);
    }

    public static boolean proxySpawnAndTick(FireworkRocketEntity entity) {
        if (!isActive) {
            return false;
        }
        entity.generation = false;
        if (entity.valid) {
            MinecraftServer.LOGGER.error("Attempted Double World add on {}", entity, new Throwable());
            return true;
        }
        if (entity.spawnReason == null) entity.spawnReason = CreatureSpawnEvent.SpawnReason.DEFAULT;
        if (entity.isRemoved()) {
            return false;
        }
        if (!org.bukkit.craftbukkit.event.CraftEventFactory.doEntityAddEventCalling(entity.level(), entity, CreatureSpawnEvent.SpawnReason.DEFAULT)) {
            return false;
        }
        proxyTickingFireworks.add(entity);
        return true;
    }

    public static void flightBehaviour(Player player, Vec3 velocity) {
        if (!LeavesConfig.modify.elytraAeronautics.noChunk) {
            return;
        }
        if ((LeavesConfig.modify.elytraAeronautics.noChunkSpeed <= 0.0D || velocity.horizontalDistanceSqr() >= LeavesConfig.modify.elytraAeronautics.noChunkSpeed)
            && (LeavesConfig.modify.elytraAeronautics.noChunkHeight <= 0.0D || player.getY() >= LeavesConfig.modify.elytraAeronautics.noChunkHeight)) {
            handleCruiseActivate(player);
        } else {
            handleCruiseDeactivate(player);
        }
    }

    public static void handleCruiseActivate(Player player) {
        if (!player.elytraAeronauticsNoChunk) {
            player.elytraAeronauticsNoChunk = true;
            ServerPlayer serverPlayer = (ServerPlayer) player;
            if (LeavesConfig.modify.elytraAeronautics.noChunkMes) {
                serverPlayer.sendSystemMessage(Component.literal(LeavesConfig.modify.elytraAeronautics.noChunkStartMes), true);
            }
            try {
                PlatformHooks.get().removePlayerFromDistanceMaps(serverPlayer.serverLevel(), serverPlayer);
                serverPlayer.serverLevel().chunkSource.chunkMap.getDistanceManager().removePlayer(serverPlayer.getLastSectionPos(), serverPlayer);
            } catch (Exception ignored) {
            }
        }
    }

    public static void handleCruiseDeactivate(Player player) {
        if (player.elytraAeronauticsNoChunk) {
            player.elytraAeronauticsNoChunk = false;
            ServerPlayer serverPlayer = (ServerPlayer) player;
            if (LeavesConfig.modify.elytraAeronautics.noChunkMes) {
                serverPlayer.sendSystemMessage(Component.literal(LeavesConfig.modify.elytraAeronautics.noChunkEndMes), true);
            }
            try {
                PlatformHooks.get().addPlayerToDistanceMaps((ServerLevel) serverPlayer.level(), serverPlayer);
                ((ServerLevel) serverPlayer.level()).chunkSource.chunkMap.getDistanceManager().addPlayer(SectionPos.of(serverPlayer), serverPlayer);
            } catch (Exception ignored) {
            }
        }
    }
}