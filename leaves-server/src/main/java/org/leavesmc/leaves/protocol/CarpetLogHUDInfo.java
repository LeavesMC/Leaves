package org.leavesmc.leaves.protocol;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@LeavesProtocol(namespace = "carpet")
public class CarpetLogHUDInfo {

    private static MobCategory[] categories;

    private static Map<CraftPlayer, Component> playersMessage = new HashMap<>();

    public static NamedTextColor creatureTypeColor(MobCategory type) {
        return switch (type) {
            case MONSTER -> NamedTextColor.DARK_RED;
            case CREATURE -> NamedTextColor.DARK_GREEN;
            case AMBIENT -> NamedTextColor.DARK_GRAY;
            case WATER_CREATURE -> NamedTextColor.DARK_BLUE;
            case WATER_AMBIENT -> NamedTextColor.DARK_AQUA;
            default -> NamedTextColor.WHITE;
        };
    }

    public static TextColor heatmapColor(double actual, double reference) {
        NamedTextColor color = NamedTextColor.GRAY;
        if (actual >= 0.0D) color = NamedTextColor.DARK_GREEN;
        if (actual > 0.5D * reference) color = NamedTextColor.YELLOW;
        if (actual > 0.8D * reference) color = NamedTextColor.RED;
        if (actual > reference) color = NamedTextColor.LIGHT_PURPLE;
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
            TextColor color = heatmapColor(mspt, msptMax);
            Component message = Component.empty()
                .append(
                    Component.text("TPS: ").color(NamedTextColor.GRAY)
                ).append(
                    Component.text(String.format("%.1f", tps)).color(color)
                ).append(
                    Component.text(" MSPT: ").color(NamedTextColor.GRAY)
                ).append(
                    Component.text(String.format("%.1f", mspt)).color(color)
                );

            // Send MobCaps Data

            Set<ServerLevel> levels = realPlayers.stream().map(ServerPlayer::serverLevel).collect(Collectors.toUnmodifiableSet());
            Map<ServerLevel, Component> serverLevelEntitiesMessageMap = levels.stream().map(world -> {
                List<Component> lst = new ArrayList<>();
                NaturalSpawner.SpawnState lastSpawner = world.getChunkSource().getLastSpawnState();
                int chunkcount = lastSpawner == null ? 0 : lastSpawner.getSpawnableChunkCount();
                Object2IntMap<MobCategory> dimCounts = lastSpawner == null ? null : lastSpawner.getMobCategoryCounts();
                if (dimCounts == null || chunkcount < 0) {
                    lst.add(Component.text("  --UNAVAILABLE--").color(NamedTextColor.GRAY));
                    return new Pair<>(world, lst);
                }
                List<Component> shortCodes = new ArrayList<>();
                for (MobCategory category : categories) {
                    int cur = lastSpawner.getMobCategoryCounts().getOrDefault(category, 0);
                    int max = NaturalSpawner.globalLimitForCategory(world, category, chunkcount);
                    TextColor mobColor = creatureTypeColor(category);
                    shortCodes.add(Component.text("" + (cur < 0 ? "-" : cur)).color(heatmapColor(cur, max)));
                    shortCodes.add(Component.text("/").color(NamedTextColor.GRAY));
                    shortCodes.add(Component.text(String.valueOf(max)).color(mobColor));
                    shortCodes.add(Component.text(",").color(NamedTextColor.GRAY));
                }
                if (!shortCodes.isEmpty()) {
                    shortCodes.removeLast();
                    lst.addAll(shortCodes);
                } else {
                    lst.add(Component.text("  --UNAVAILABLE--").color(NamedTextColor.GRAY));
                }
                return new Pair<>(world, lst);
            }).map(e -> {
                List<Component> b = e.getB();
                Component full = Component.join(JoinConfiguration.builder().build(), b);
                return new Pair<>(e.getA(), full);
            }).collect(Collectors.toMap(Pair::getA, Pair::getB));


            // send data to tab bar
            realPlayers.forEach(player -> {
                ServerLevel serverLevel = player.serverLevel();
                Component components = serverLevelEntitiesMessageMap.get(serverLevel);
                CraftPlayer bukkitEntity = player.getBukkitEntity();
                Component currentPlayerData = Component.join(JoinConfiguration.newlines(), message, components);
                playersMessage.put(bukkitEntity, currentPlayerData);
                bukkitEntity.leaves$refreshPlayerListHeaderAndFooter();
            });
        }
    }

    public static Component getPlayerMessage(CraftPlayer player) {
        return playersMessage.get(player);
    }
}
