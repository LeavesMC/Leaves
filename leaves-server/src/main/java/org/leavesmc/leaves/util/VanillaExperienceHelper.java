package org.leavesmc.leaves.util;

import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.configuration.WorldConfiguration;
import io.papermc.paper.configuration.type.number.IntOr;
import org.leavesmc.leaves.LeavesConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VanillaExperienceHelper {

    private static final List<Consumer<WorldConfiguration>> worldConfigModifiers = new ArrayList<>();

    public static void doVanillaExperienceIf() {
        if (LeavesConfig.modify.vanillaExperience) {
            doVanillaExperience();
        }
    }

    public static void doVanillaExperience() {
        // paper-global.yml
        GlobalConfiguration.get().commands.suggestPlayerNamesWhenNullTabCompletions = false;
        GlobalConfiguration.get().commands.timeCommandAffectsAllWorlds = true;
        GlobalConfiguration.get().itemValidation.displayName = 2147483647;
        GlobalConfiguration.get().itemValidation.loreLine = 2147483647;
        GlobalConfiguration.get().itemValidation.resolveSelectorsInBooks = true;
        GlobalConfiguration.get().itemValidation.book.author = 2147483647;
        GlobalConfiguration.get().itemValidation.book.page = 2147483647;
        GlobalConfiguration.get().itemValidation.book.title = 2147483647;
        GlobalConfiguration.get().itemValidation.bookSize.pageMax = IntOr.Disabled.DISABLED;
        GlobalConfiguration.get().misc.maxJoinsPerTick = 2147483647;
        GlobalConfiguration.get().packetLimiter.allPackets = new GlobalConfiguration.PacketLimiter.PacketLimit(0.000001, 999999.0, GlobalConfiguration.get().packetLimiter.allPackets.action());
        GlobalConfiguration.get().packetLimiter.overrides = Map.of();
        GlobalConfiguration.get().spamLimiter.incomingPacketThreshold = IntOr.Disabled.DISABLED;
        GlobalConfiguration.get().unsupportedSettings.allowHeadlessPistons = true;
        GlobalConfiguration.get().unsupportedSettings.allowPermanentBlockBreakExploits = true;
        GlobalConfiguration.get().unsupportedSettings.allowPistonDuplication = true;
        GlobalConfiguration.get().unsupportedSettings.performUsernameValidation = false;
        GlobalConfiguration.get().unsupportedSettings.allowUnsafeEndPortalTeleportation = true;
        GlobalConfiguration.get().unsupportedSettings.skipTripwireHookPlacementValidation = true;

        // paper-world-defaults.yml
        worldConfigModifiers.add(config -> config.chunks.delayChunkUnloadsBy = io.papermc.paper.configuration.type.Duration.of("0s"));
        worldConfigModifiers.add(config -> config.chunks.maxAutoSaveChunksPerTick = 200);
        worldConfigModifiers.add(config -> config.collisions.allowPlayerCrammingDamage = true);
        worldConfigModifiers.add(config -> config.collisions.maxEntityCollisions = 2147483647);
        worldConfigModifiers.add(config -> config.entities.behavior.onlyMergeItemsHorizontally = true);
        worldConfigModifiers.add(config -> config.entities.behavior.phantomsDoNotSpawnOnCreativePlayers = false);
        worldConfigModifiers.add(config -> config.entities.behavior.phantomsOnlyAttackInsomniacs = false);
        worldConfigModifiers.add(config -> config.entities.behavior.cooldownFailedBeehiveReleases = false);
        worldConfigModifiers.add(config -> config.entities.spawning.countAllMobsForSpawning = true);
        worldConfigModifiers.add(config -> config.entities.spawning.perPlayerMobSpawns = false);
        worldConfigModifiers.add(config -> config.entities.spawning.filterBadTileEntityNbtFromFallingBlocks = false);
        worldConfigModifiers.add(config -> config.entities.spawning.filteredEntityTagNbtPaths = List.of());
        worldConfigModifiers.add(config -> config.hopper.cooldownWhenFull = false);
        worldConfigModifiers.add(config -> config.maps.itemFrameCursorLimit = 2147483647);
        worldConfigModifiers.add(config -> config.scoreboards.useVanillaWorldScoreboardNameColoring = true);
        worldConfigModifiers.add(config -> config.unsupportedSettings.disableWorldTickingWhenEmpty = true);
        worldConfigModifiers.add(config -> config.unsupportedSettings.fixInvulnerableEndCrystalExploit = false);
    }

    public static void onWorldConfigCreate(WorldConfiguration config) {
        if (LeavesConfig.modify.vanillaExperience) {
            worldConfigModifiers.forEach(it -> it.accept(config));
        }
    }
}