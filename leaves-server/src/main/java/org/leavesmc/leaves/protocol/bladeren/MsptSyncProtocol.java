package org.leavesmc.leaves.protocol.bladeren;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

@LeavesProtocol(namespace = "bladeren")
public class MsptSyncProtocol {

    public static final String PROTOCOL_ID = "bladeren";

    private static final ResourceLocation MSPT_SYNC = id("mspt_sync");

    private static final List<ServerPlayer> players = new ArrayList<>();

    @Contract("_ -> new")
    public static @NotNull ResourceLocation id(String path) {
        return new ResourceLocation(PROTOCOL_ID, path);
    }

    @ProtocolHandler.Init
    public static void init() {
        BladerenProtocol.registerFeature("mspt_sync", (player, compoundTag) -> {
            if (compoundTag.getString("Value").equals("true")) {
                onPlayerSubmit(player);
            } else {
                onPlayerLoggedOut(player);
            }
        });
    }

    @ProtocolHandler.PlayerLeave
    public static void onPlayerLoggedOut(@NotNull ServerPlayer player) {
        if (LeavesConfig.protocol.bladeren.msptSyncProtocol) {
            players.remove(player);
        }
    }

    @ProtocolHandler.Ticker
    public static void tick() {
        if (LeavesConfig.protocol.bladeren.msptSyncProtocol) {
            if (players.isEmpty()) {
                return;
            }

            MinecraftServer server = MinecraftServer.getServer();
            if (server.getTickCount() % LeavesConfig.protocol.bladeren.msptSyncTickInterval == 0) {
                OptionalDouble msptArr = Arrays.stream(server.getTickTimesNanos()).average();
                if (msptArr.isPresent()) {
                    double mspt = msptArr.getAsDouble() * 1.0E-6D;
                    double tps = 1000.0D / Math.max(mspt, 50);
                    players.forEach(player -> ProtocolUtils.sendPayloadPacket(player, MSPT_SYNC, buf -> {
                        buf.writeDouble(mspt);
                        buf.writeDouble(tps);
                    }));
                }
            }
        }
    }

    public static void onPlayerSubmit(@NotNull ServerPlayer player) {
        if (LeavesConfig.protocol.bladeren.msptSyncProtocol) {
            players.add(player);
        }
    }
}
