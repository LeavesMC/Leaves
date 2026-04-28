package org.leavesmc.leaves.util;

import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.configuration.WorldConfiguration;
import io.papermc.paper.configuration.type.number.IntOr;
import org.leavesmc.leaves.LeavesConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class McTechnicalModeHelper {

    private static final List<Consumer<WorldConfiguration>> worldConfigModifiers = new ArrayList<>();

    public static void doMcTechnicalModeIf() {
        if (LeavesConfig.modify.mcTechnicalMode) {
            doMcTechnicalMode();
        }
    }

    public static void doMcTechnicalMode() {
        GlobalConfiguration.get().unsupportedSettings.allowPistonDuplication = true;
        GlobalConfiguration.get().unsupportedSettings.allowHeadlessPistons = true;
        GlobalConfiguration.get().unsupportedSettings.allowPermanentBlockBreakExploits = true;
        GlobalConfiguration.get().unsupportedSettings.allowUnsafeEndPortalTeleportation = true;
        GlobalConfiguration.get().unsupportedSettings.skipTripwireHookPlacementValidation = true;
        GlobalConfiguration.get().packetLimiter.allPackets = new GlobalConfiguration.PacketLimiter.PacketLimit(GlobalConfiguration.get().packetLimiter.allPackets.interval(),
            5000.0, GlobalConfiguration.get().packetLimiter.allPackets.action());
        GlobalConfiguration.get().packetLimiter.overrides = Map.of();
        GlobalConfiguration.get().itemValidation.resolveSelectorsInBooks = true;
        GlobalConfiguration.get().scoreboards.saveEmptyScoreboardTeams = true;
        worldConfigModifiers.add(config -> config.entities.spawning.maxArrowDespawnInvulnerability = IntOr.Disabled.DISABLED);
    }

    public static void onWorldConfigCreate(WorldConfiguration config) {
        if (LeavesConfig.modify.mcTechnicalMode) {
            worldConfigModifiers.forEach(it -> it.accept(config));
        }
    }
}
