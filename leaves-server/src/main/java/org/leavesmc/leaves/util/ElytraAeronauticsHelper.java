package org.leavesmc.leaves.util;

import ca.spottedleaf.moonrise.common.PlatformHooks;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.leavesmc.leaves.LeavesConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ElytraAeronauticsHelper {

    public static final Map<ServerLevel, List<FireworkRocketEntity>> globalTickingFireworks = new HashMap<>();

    public static void tick(ServerLevel level) {
        List<FireworkRocketEntity> tickingFireworks = globalTickingFireworks.getOrDefault(level, Collections.emptyList());
        for (Iterator<FireworkRocketEntity> it = tickingFireworks.iterator(); it.hasNext(); ) {
            FireworkRocketEntity firework = it.next();
            firework.life++;
            Vec3 handHoldingItemAngle;
            if (!firework.isAlive()) {
                it.remove();
            }
            if (firework.attachedToEntity == null || firework.life > firework.lifetime) {
                explodeNoClip(firework);
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
    }

    private static void explodeNoClip(FireworkRocketEntity firework) {
        if (firework.life <= firework.lifetime) {
            firework.discard();
        }
        ServerLevel level = firework.level().getMinecraftWorld();
        List<FireworkExplosion> explosions = firework.getExplosions();
        if (!explosions.isEmpty() && firework.attachedToEntity != null &&
            org.bukkit.craftbukkit.event.CraftEventFactory.callFireworkExplodeEvent(firework)) {
            level.broadcastEntityEvent(firework, (byte) 17);
            firework.gameEvent(GameEvent.EXPLODE, firework.getOwner());
            firework.attachedToEntity.hurtServer(level, firework.damageSources().fireworks(firework, firework.getOwner()), 5.0F + explosions.size() * 2);
            firework.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.EXPLODE);
        }
    }

    public static boolean proxySpawnAndTick(FireworkRocketEntity entity) {
        if (!LeavesConfig.modify.elytraAeronautics.enableNoChunkLoad) {
            return false;
        }
        entity.generation = false;
        if (entity.valid) {
            MinecraftServer.LOGGER.error("Attempted Double World add on {}", entity, new Throwable());
            return true;
        }
        if (entity.spawnReason == null) {
            entity.spawnReason = CreatureSpawnEvent.SpawnReason.DEFAULT;
        }
        if (entity.isRemoved()) {
            return false;
        }
        if (!org.bukkit.craftbukkit.event.CraftEventFactory.doEntityAddEventCalling(entity.level(), entity, CreatureSpawnEvent.SpawnReason.DEFAULT)) {
            return false;
        }
        globalTickingFireworks.computeIfAbsent(entity.level().getMinecraftWorld(), w -> new ArrayList<>()).add(entity);
        return true;
    }

    public static void flightBehaviour(Player player, Vec3 velocity) {
        if (!LeavesConfig.modify.elytraAeronautics.enableNoChunkLoad) {
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
            if (LeavesConfig.modify.elytraAeronautics.doSendMessages) {
                serverPlayer.sendSystemMessage(Component.literal(LeavesConfig.modify.elytraAeronautics.startMessage), true);
            }
            try {
                PlatformHooks.get().removePlayerFromDistanceMaps(serverPlayer.level(), serverPlayer);
                serverPlayer.level().chunkSource.chunkMap.getDistanceManager().removePlayer(serverPlayer.getLastSectionPos(), serverPlayer);
            } catch (Exception ignored) {
            }
        }
    }

    public static void handleCruiseDeactivate(Player player) {
        if (player.elytraAeronauticsNoChunk) {
            player.elytraAeronauticsNoChunk = false;
            ServerPlayer serverPlayer = (ServerPlayer) player;
            if (LeavesConfig.modify.elytraAeronautics.doSendMessages) {
                serverPlayer.sendSystemMessage(Component.literal(LeavesConfig.modify.elytraAeronautics.endMessage), true);
            }
            try {
                PlatformHooks.get().addPlayerToDistanceMaps(serverPlayer.level(), serverPlayer);
                serverPlayer.level().chunkSource.chunkMap.getDistanceManager().addPlayer(SectionPos.of(serverPlayer), serverPlayer);
            } catch (Exception ignored) {
            }
        }
    }
}