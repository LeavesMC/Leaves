package org.leavesmc.leaves.util;

import ca.spottedleaf.moonrise.common.PlatformHooks;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.leavesmc.leaves.LeavesConfig;

public class ElytraAeronauticsHelper {

    public static void flightBehaviour(Player player, Vec3 velocity) {
        if (LeavesConfig.modify.elytraAeronautics.noChunk) {
            if ((LeavesConfig.modify.elytraAeronautics.noChunkSpeed <= 0.0D || velocity.horizontalDistanceSqr() >= LeavesConfig.modify.elytraAeronautics.noChunkSpeed)
                    && (LeavesConfig.modify.elytraAeronautics.noChunkHeight <= 0.0D || player.getY() >= LeavesConfig.modify.elytraAeronautics.noChunkHeight)) {
                if (!player.elytraAeronauticsNoChunk) {
                    player.elytraAeronauticsNoChunk = true;
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    if (LeavesConfig.modify.elytraAeronautics.noChunkMes) {
                        serverPlayer.sendSystemMessage(Component.literal(LeavesConfig.modify.elytraAeronautics.noChunkStartMes), true);
                    }
                    PlatformHooks.get().removePlayerFromDistanceMaps(serverPlayer.serverLevel(), serverPlayer);
                    serverPlayer.serverLevel().chunkSource.chunkMap.getDistanceManager().removePlayer(serverPlayer.getLastSectionPos(), serverPlayer);
                }
            } else {
                if (player.elytraAeronauticsNoChunk) {
                    player.elytraAeronauticsNoChunk = false;
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    if (LeavesConfig.modify.elytraAeronautics.noChunkMes) {
                        serverPlayer.sendSystemMessage(Component.literal(LeavesConfig.modify.elytraAeronautics.noChunkEndMes), true);
                    }
                    PlatformHooks.get().addPlayerToDistanceMaps((ServerLevel) serverPlayer.level(), serverPlayer);
                    ((ServerLevel) serverPlayer.level()).chunkSource.chunkMap.getDistanceManager().addPlayer(SectionPos.of(serverPlayer), serverPlayer);
                }
            }
        }
    }
}
