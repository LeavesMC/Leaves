package org.leavesmc.leaves.util;

import io.papermc.paper.configuration.GlobalConfiguration;
import org.leavesmc.leaves.LeavesConfig;

import java.util.Map;

public class McTechnicalModeHelper {

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
    }
}
