package org.leavesmc.leaves.protocol.bladeren;

import net.minecraft.resources.Identifier;
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

@LeavesProtocol.Register(namespace = "bladeren")
public class MsptSyncProtocol implements LeavesProtocol {

    public static final String PROTOCOL_ID = "bladeren";
    private static final Identifier MSPT_SYNC = id("mspt_sync");
    private static final List<ServerPlayer> players = new ArrayList<>();

    @Contract("_ -> new")
    public static Identifier id(String path) {
        return Identifier.tryBuild(PROTOCOL_ID, path);
    }

    @ProtocolHandler.Init
    public static void init() {
        BladerenProtocol.registerFeature("mspt_sync", (player, compoundTag) -> {
            if (compoundTag.getStringOr("Value", "").equals("true")) {
                onPlayerSubmit(player);
            } else {
                onPlayerLoggedOut(player);
            }
        });
    }

    @ProtocolHandler.PlayerLeave
    public static void onPlayerLoggedOut(@NotNull ServerPlayer player) {
        players.remove(player);
    }

    @ProtocolHandler.Ticker
    public static void tick() {
        if (players.isEmpty()) {
            return;
        }
        MinecraftServer server = MinecraftServer.getServer();
        OptionalDouble msptArr = Arrays.stream(server.getTickTimesNanos()).average();
        if (msptArr.isPresent()) {
            double mspt = msptArr.getAsDouble() * 1.0E-6D;
            double tps = 1000.0D / Math.max(mspt, 50);
            players.forEach(player -> ProtocolUtils.sendBytebufPacket(player, MSPT_SYNC, buf -> {
                buf.writeDouble(mspt);
                buf.writeDouble(tps);
            }));
        }
    }

    public static void onPlayerSubmit(@NotNull ServerPlayer player) {
        players.add(player);
    }

    @Override
    public int tickerInterval(String tickerID) {
        return LeavesConfig.protocol.bladeren.msptSyncTickInterval;
    }

    @Override
    public boolean isActive() {
        return LeavesConfig.protocol.bladeren.msptSyncProtocol;
    }
}
