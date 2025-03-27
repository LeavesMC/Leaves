package org.leavesmc.leaves.protocol;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TimeUtil;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;

import java.util.List;

@LeavesProtocol(namespace = "carpet")
public class CarpetLogHUDInfo {
    @ProtocolHandler.Ticker
    public static void onTick() {
        if (LeavesConfig.protocol.carpetMSPTMobCapsSupport) {
            MinecraftServer server = MinecraftServer.getServer();
            List<ServerPlayer> realPlayers = server.getPlayerList().realPlayers;
            if (realPlayers.isEmpty()) return;
            ServerTickRateManager tickRateManager = server.tickRateManager();
            double mspt = (double) server.getAverageTickTimeNanos() / TimeUtil.NANOSECONDS_PER_MILLISECOND;
            double tps = 1000.0D / Math.max(tickRateManager.isSprinting() ? 0 : tickRateManager.millisecondsPerTick(), mspt);
            MutableComponent message = Component.empty()
                .append(
                    Component.literal("TPS: ").withStyle(ChatFormatting.GRAY)
                ).append(
                    Component.literal(String.format("%.1f", tps)).withStyle(ChatFormatting.GREEN)
                ).append(
                    Component.literal(" MSPT: ").withStyle(ChatFormatting.GRAY)
                ).append(
                    Component.literal(String.format("%.1f", mspt)).withStyle(ChatFormatting.GREEN)
                );
            realPlayers.forEach(player -> {
                ClientboundTabListPacket packet = new ClientboundTabListPacket(Component.empty(), message);
                player.connection.send(packet);
            });
        }
    }
}
