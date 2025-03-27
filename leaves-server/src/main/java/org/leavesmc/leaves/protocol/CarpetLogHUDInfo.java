package org.leavesmc.leaves.protocol;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@LeavesProtocol(namespace = "carpet")
public class CarpetLogHUDInfo {

    private static MobCategory[] categories;

    public static ChatFormatting creatureTypeColor(MobCategory type) {
        return switch (type) {
            case MONSTER -> ChatFormatting.DARK_RED;
            case CREATURE -> ChatFormatting.DARK_GREEN;
            case AMBIENT -> ChatFormatting.DARK_GRAY;
            case WATER_CREATURE -> ChatFormatting.DARK_BLUE;
            case WATER_AMBIENT -> ChatFormatting.DARK_AQUA;
            default -> ChatFormatting.WHITE;
        };
    }

    public static ChatFormatting heatmapColor(double actual, double reference) {
        ChatFormatting color = ChatFormatting.GRAY;
        if (actual >= 0.0D) color = ChatFormatting.DARK_GREEN;
        if (actual > 0.5D * reference) color = ChatFormatting.YELLOW;
        if (actual > 0.8D * reference) color = ChatFormatting.RED;
        if (actual > reference) color = ChatFormatting.LIGHT_PURPLE;
        return color;
    }


    @ProtocolHandler.Init
    public static void init() {
        categories = MobCategory.values();
    }

    @ProtocolHandler.Ticker
    public static void onTick() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server.getTickCount() % 20 != 0) return;
        if (LeavesConfig.protocol.carpetMSPTMobCapsSupport) {
            // Send TPS/MSPT Data
            List<ServerPlayer> realPlayers = server.getPlayerList().realPlayers;
            if (realPlayers.isEmpty()) return;
            ServerTickRateManager tickRateManager = server.tickRateManager();
            double mspt = (double) server.getAverageTickTimeNanos() / TimeUtil.NANOSECONDS_PER_MILLISECOND;
            float msptMax = tickRateManager.millisecondsPerTick();
            double tps = 1000.0D / Math.max(tickRateManager.isSprinting() ? 0 : msptMax, mspt);
            ChatFormatting color = heatmapColor(mspt, msptMax);
            MutableComponent message = Component.empty()
                .append(
                    Component.literal("TPS: ").withStyle(ChatFormatting.GRAY)
                ).append(
                    Component.literal(String.format("%.1f", tps)).withStyle(color)
                ).append(
                    Component.literal(" MSPT: ").withStyle(ChatFormatting.GRAY)
                ).append(
                    Component.literal(String.format("%.1f", mspt)).withStyle(color)
                );

            // MobCategory


            // Send MobCaps Data

            Set<ServerLevel> levels = realPlayers.stream().map(ServerPlayer::serverLevel).collect(Collectors.toUnmodifiableSet());
            Map<ServerLevel, MutableComponent> serverLevelEntitiesMessageMap = levels.stream().map(world -> {
                List<Component> lst = new ArrayList<>();
                NaturalSpawner.SpawnState lastSpawner = world.getChunkSource().getLastSpawnState();
                int chunkcount = lastSpawner == null ? 0 : lastSpawner.getSpawnableChunkCount();
                Object2IntMap<MobCategory> dimCounts = lastSpawner == null ? null : lastSpawner.getMobCategoryCounts();
                if (dimCounts == null || chunkcount < 0) {
                    lst.add(Component.literal("  --UNAVAILABLE--").withStyle(ChatFormatting.GRAY));
                    return new Pair<>(world, lst);
                }
                List<Component> shortCodes = new ArrayList<>();
                for (MobCategory category : categories) {
                    int cur = lastSpawner.getMobCategoryCounts().getOrDefault(category, 0);
                    int max = NaturalSpawner.globalLimitForCategory(world, category, chunkcount);
                    ChatFormatting mobColor = creatureTypeColor(category);
                    shortCodes.add(Component.literal("" + (cur < 0 ? "-" : cur)).withStyle(heatmapColor(cur, max)));
                    shortCodes.add(Component.literal("/").withStyle(ChatFormatting.GRAY));
                    shortCodes.add(Component.literal(String.valueOf(max)).withStyle(mobColor));
                    shortCodes.add(Component.literal(",").withStyle(ChatFormatting.GRAY));
                }
                if (!shortCodes.isEmpty()) {
                    shortCodes.removeLast();
                    lst.addAll(shortCodes);
                } else {
                    lst.add(Component.literal("  --UNAVAILABLE--").withStyle(ChatFormatting.GRAY));
                }
                return new Pair<>(world, lst);
            }).map(e -> {
                List<Component> b = e.getB();
                MutableComponent full = Component.empty();
                b.forEach(full::append);
                return new Pair<>(e.getA(), full);
            }).collect(Collectors.toMap(Pair::getA, Pair::getB));


            // send data to tab bar
            realPlayers.forEach(player -> {
                ServerLevel serverLevel = player.serverLevel();
                MutableComponent components = serverLevelEntitiesMessageMap.get(serverLevel);
                ClientboundTabListPacket packet = new ClientboundTabListPacket(Component.empty(), Component.empty().append(message).append(Component.literal("\n")).append(components));
                player.connection.send(packet);
            });
        }
    }
}
