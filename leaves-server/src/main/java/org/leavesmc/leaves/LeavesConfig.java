package org.leavesmc.leaves;

import com.destroystokyo.paper.util.SneakyThrow;
import io.papermc.paper.configuration.GlobalConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.command.LeavesCommand;
import org.leavesmc.leaves.config.annotations.GlobalConfig;
import org.leavesmc.leaves.config.annotations.GlobalConfigCategory;
import org.leavesmc.leaves.config.annotations.RemovedConfig;
import org.leavesmc.leaves.config.GlobalConfigManager;
import org.leavesmc.leaves.region.RegionFileFormat;
import org.leavesmc.leaves.util.MathUtils;

import org.leavesmc.leaves.config.ConfigValidatorImpl.BooleanConfigValidator;
import org.leavesmc.leaves.config.ConfigValidatorImpl.IntConfigValidator;
import org.leavesmc.leaves.config.ConfigValidatorImpl.StringConfigValidator;
import org.leavesmc.leaves.config.ConfigValidatorImpl.DoubleConfigValidator;
import org.leavesmc.leaves.config.ConfigValidatorImpl.ListConfigValidator;
import org.leavesmc.leaves.config.ConfigValidatorImpl.EnumConfigValidator;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.leavesmc.leaves.protocol.CarpetServerProtocol.CarpetRule;
import org.leavesmc.leaves.protocol.CarpetServerProtocol.CarpetRules;

import org.leavesmc.leaves.protocol.bladeren.BladerenProtocol.LeavesFeatureSet;
import org.leavesmc.leaves.protocol.bladeren.BladerenProtocol.LeavesFeature;

public final class LeavesConfig {

    public static final String CONFIG_HEADER = "Configuration file for Leaves.";
    public static final int CURRENT_CONFIG_VERSION = 6;

    private static File configFile;
    public static YamlConfiguration config;

    public static void init(final @NotNull File file) {
        LeavesConfig.configFile = file;
        config = new YamlConfiguration();
        config.options().setHeader(Collections.singletonList(CONFIG_HEADER));
        config.options().copyDefaults(true);

        if (!file.exists()) {
            try {
                boolean is = file.createNewFile();
                if (!is) {
                    throw new IOException("Can't create file");
                }
            } catch (final Exception ex) {
                LeavesLogger.LOGGER.severe("Failure to create leaves config", ex);
            }
        } else {
            try {
                config.load(file);
            } catch (final Exception ex) {
                LeavesLogger.LOGGER.severe("Failure to load leaves config", ex);
                SneakyThrow.sneaky(ex);
                throw new RuntimeException(ex);
            }
        }

        LeavesConfig.config.set("config-version", CURRENT_CONFIG_VERSION);

        GlobalConfigManager.init();

        registerCommand("leaves", new LeavesCommand("leaves"));
    }

    public static void reload() {
        if (!LeavesConfig.configFile.exists()) {
            throw new RuntimeException("Leaves config file not found, please restart the server");
        }

        try {
            config.load(LeavesConfig.configFile);
        } catch (final Exception ex) {
            LeavesLogger.LOGGER.severe("Failure to reload leaves config", ex);
            SneakyThrow.sneaky(ex);
            throw new RuntimeException(ex);
        }

        GlobalConfigManager.reload();
    }

    public static void save() {
        try {
            config.save(LeavesConfig.configFile);
        } catch (final Exception ex) {
            LeavesLogger.LOGGER.severe("Unable to save leaves config", ex);
        }
    }

    public static void registerCommand(String name, Command command) {
        MinecraftServer.getServer().server.getCommandMap().register(name, "leaves", command);
        MinecraftServer.getServer().server.syncCommands();
    }

    public static void unregisterCommand(String name) {
        name = name.toLowerCase(Locale.ENGLISH).trim();
        MinecraftServer.getServer().server.getCommandMap().getKnownCommands().remove(name);
        MinecraftServer.getServer().server.getCommandMap().getKnownCommands().remove("leaves:" + name);
        MinecraftServer.getServer().server.syncCommands();
    }

    public static ModifyConfig modify = new ModifyConfig();

    @GlobalConfigCategory("modify")
    public static class ModifyConfig {

        public FakeplayerConfig fakeplayer = new FakeplayerConfig();

        @GlobalConfigCategory("fakeplayer")
        public static class FakeplayerConfig {

            @RemovedConfig(name = "enable", category = "fakeplayer", transform = true)
            @GlobalConfig(value = "enable", validator = FakeplayerValidator.class)
            public boolean enable = true;

            private static class FakeplayerValidator extends BooleanConfigValidator {
                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    if (value) {
                        registerCommand("bot", new org.leavesmc.leaves.bot.BotCommand("bot"));
                        org.leavesmc.leaves.bot.agent.Actions.registerAll();
                    } else {
                        unregisterCommand("bot");
                    }
                }
            }

            @RemovedConfig(name = "unable-fakeplayer-names", category = "fakeplayer", transform = true)
            @GlobalConfig(value = "unable-fakeplayer-names")
            public List<String> unableNames = List.of("player-name");

            @GlobalConfig(value = "limit")
            public int limit = 10;

            @GlobalConfig(value = "prefix")
            public String prefix = "";

            @GlobalConfig(value = "suffix")
            public String suffix = "";

            @GlobalConfig(value = "regen-amount", validator = RegenAmountValidator.class)
            public double regenAmount = 0.0;

            private static class RegenAmountValidator extends DoubleConfigValidator {
                @Override
                public void verify(Double old, Double value) throws IllegalArgumentException {
                    if (value < 0.0) {
                        throw new IllegalArgumentException("regen-amount need >= 0.0");
                    }
                }
            }

            @GlobalConfig("always-send-data")
            public boolean canSendDataAlways = true;

            @GlobalConfig("resident-fakeplayer")
            public boolean canResident = false;

            @GlobalConfig("open-fakeplayer-inventory")
            public boolean canOpenInventory = false;

            @GlobalConfig("skip-sleep-check")
            public boolean canSkipSleep = false;

            @GlobalConfig("spawn-phantom")
            public boolean canSpawnPhantom = false;

            @GlobalConfig("use-action")
            public boolean canUseAction = true;

            @GlobalConfig("modify-config")
            public boolean canModifyConfig = false;

            @GlobalConfig("manual-save-and-load")
            public boolean canManualSaveAndLoad = false;

            @GlobalConfig(value = "cache-skin", lock = true)
            public boolean useSkinCache = false;
        }

        public MinecraftOLDConfig oldMC = new MinecraftOLDConfig();

        @GlobalConfigCategory("minecraft-old")
        public static class MinecraftOLDConfig {

            public BlockUpdaterConfig updater = new BlockUpdaterConfig();

            @GlobalConfigCategory("block-updater")
            public static class BlockUpdaterConfig {
                @RemovedConfig(name = "instant-block-updater-reintroduced", category = "modify", transform = true)
                @RemovedConfig(name = "instant-block-updater-reintroduced", category = {"modify", "minecraft-old"}, transform = true)
                @GlobalConfig(value = "instant-block-updater-reintroduced", lock = true)
                public boolean instantBlockUpdaterReintroduced = false;

                @RemovedConfig(name = "cce-update-suppression", category = {"modify", "minecraft-old"}, transform = true)
                @GlobalConfig("cce-update-suppression")
                public boolean cceUpdateSuppression = false;

                @RemovedConfig(name = "redstone-wire-dont-connect-if-on-trapdoor", category = "modify", transform = true)
                @RemovedConfig(name = "redstone-wire-dont-connect-if-on-trapdoor", category = {"modify", "minecraft-old"}, transform = true)
                @GlobalConfig("redstone-wire-dont-connect-if-on-trapdoor")
                public boolean redstoneDontCantOnTrapDoor = false;

                @RemovedConfig(name = "old-block-entity-behaviour", category = {"modify", "minecraft-old"}, transform = true)
                @GlobalConfig("old-block-entity-behaviour")
                public boolean oldBlockEntityBehaviour = false;
            }

            @RemovedConfig(name = "shears-in-dispenser-can-zero-amount", category = {}, transform = true)
            @RemovedConfig(name = "shears-in-dispenser-can-zero-amount", category = "modify", transform = true)
            @GlobalConfig("shears-in-dispenser-can-zero-amount")
            public boolean shearsInDispenserCanZeroAmount = false;

            @GlobalConfig("armor-stand-cant-kill-by-mob-projectile")
            public boolean armorStandCantKillByMobProjectile = false;

            @GlobalConfig(value = "villager-infinite-discounts", validator = VillagerInfiniteDiscountsValidator.class)
            public boolean villagerInfiniteDiscounts = false;

            private static class VillagerInfiniteDiscountsValidator extends BooleanConfigValidator {
                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    org.leavesmc.leaves.util.VillagerInfiniteDiscountHelper.doVillagerInfiniteDiscount(value);
                }
            }

            @GlobalConfig("copper-bulb-1gt-delay")
            public boolean copperBulb1gt = false;

            @GlobalConfig("crafter-1gt-delay")
            public boolean crafter1gt = false;

            @RemovedConfig(name = "zero-tick-plants", category = "modify", transform = true)
            @GlobalConfig("zero-tick-plants")
            public boolean zeroTickPlants = false;

            @RemovedConfig(name = "loot-world-random", category = {"modify", "minecraft-old"}, transform = true)
            @GlobalConfig(value = "rng-fishing", lock = true, validator = RNGFishingValidator.class)
            public boolean rngFishing = false;

            private static class RNGFishingValidator extends BooleanConfigValidator {
                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    LeavesFeatureSet.register(LeavesFeature.of("rng_fishing", value));
                }
            }

            @GlobalConfig("allow-grindstone-overstacking")
            public boolean allowGrindstoneOverstacking = false;

            @GlobalConfig("allow-entity-portal-with-passenger")
            public boolean allowEntityPortalWithPassenger = true;

            @GlobalConfig("disable-gateway-portal-entity-ticking")
            public boolean disableGatewayPortalEntityTicking = false;

            @GlobalConfig("disable-LivingEntity-ai-step-alive-check")
            public boolean disableLivingEntityAiStepAliveCheck = false;

            @GlobalConfig("fix-fortress-mob-spawn")
            public boolean fixFortressMobSpawn = false;

            @GlobalConfig("old-hopper-suck-in-behavior")
            public boolean oldHopperSuckInBehavior = false;

            public RaidConfig raid = new RaidConfig();

            @GlobalConfigCategory("revert-raid-changes")
            public static class RaidConfig {
                @GlobalConfig("allow-bad-omen-trigger-raid")
                public boolean allowBadOmenTriggerRaid = false;

                @GlobalConfig("give-bad-omen-when-kill-patrol-leader")
                public boolean giveBadOmenWhenKillPatrolLeader = false;
            }

            @GlobalConfig("allow-anvil-destroy-item-entities")
            public boolean allowAnvilDestroyItemEntities = false;

            @GlobalConfig("string-tripwire-hook-duplicate")
            public boolean stringTripwireHookDuplicate = false;
        }

        public ElytraAeronauticsConfig elytraAeronautics = new ElytraAeronauticsConfig();

        @GlobalConfigCategory("elytra-aeronautics")
        public static class ElytraAeronauticsConfig {
            @GlobalConfig("no-chunk-load")
            public boolean noChunk = false;

            @GlobalConfig(value = "no-chunk-height")
            public double noChunkHeight = 500.0D;

            @GlobalConfig(value = "no-chunk-speed")
            public double noChunkSpeed = -1.0D;

            @GlobalConfig("message")
            public boolean noChunkMes = true;

            @GlobalConfig(value = "message-start")
            public String noChunkStartMes = "Flight enter cruise mode";

            @GlobalConfig(value = "message-end")
            public String noChunkEndMes = "Flight exit cruise mode";
        }

        @RemovedConfig(name = "redstone-shears-wrench", category = {}, transform = true)
        @GlobalConfig("redstone-shears-wrench")
        public boolean redstoneShearsWrench = true;

        @RemovedConfig(name = "budding-amethyst-can-push-by-piston", category = {}, transform = true)
        @GlobalConfig("budding-amethyst-can-push-by-piston")
        public boolean buddingAmethystCanPushByPiston = false;

        @RemovedConfig(name = "spectator-dont-get-advancement", category = {}, transform = true)
        @GlobalConfig("spectator-dont-get-advancement")
        public boolean spectatorDontGetAdvancement = false;

        @RemovedConfig(name = "stick-change-armorstand-arm-status", category = {}, transform = true)
        @GlobalConfig("stick-change-armorstand-arm-status")
        public boolean stickChangeArmorStandArmStatus = true;

        @RemovedConfig(name = "snowball-and-egg-can-knockback-player", category = {}, transform = true)
        @GlobalConfig("snowball-and-egg-can-knockback-player")
        public boolean snowballAndEggCanKnockback = true;

        @GlobalConfig("flatten-triangular-distribution")
        public boolean flattenTriangularDistribution = false;

        @GlobalConfig("player-operation-limiter")
        public boolean playerOperationLimiter = false;

        @GlobalConfig(value = "renewable-elytra", validator = RenewableElytraValidator.class)
        public double renewableElytra = -1.0F;

        private static class RenewableElytraValidator extends DoubleConfigValidator {
            @Override
            public void verify(Double old, Double value) throws IllegalArgumentException {
                if (value > 1.0) {
                    throw new IllegalArgumentException("renewable-elytra need <= 1.0f");
                }
            }
        }

        public int shulkerBoxStackSize = 1;
        @GlobalConfig(value = "stackable-shulker-boxes", validator = StackableShulkerValidator.class)
        private String stackableShulkerBoxes = "false";

        private static class StackableShulkerValidator extends StringConfigValidator {
            @Override
            public void verify(String old, String value) throws IllegalArgumentException {
                String realValue = MathUtils.isNumeric(value) ? value : value.equals("true") ? "2" : "1";
                LeavesConfig.modify.shulkerBoxStackSize = Integer.parseInt(realValue);
            }
        }

        @GlobalConfig("force-void-trade")
        public boolean forceVoidTrade = false;

        @GlobalConfig(value = "mc-technical-survival-mode", validator = McTechnicalModeValidator.class, lock = true)
        public boolean mcTechnicalMode = true;

        private static class McTechnicalModeValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                if (value) {
                    org.leavesmc.leaves.util.McTechnicalModeHelper.doMcTechnicalMode();
                }
            }
        }

        @GlobalConfig("return-nether-portal-fix")
        public boolean netherPortalFix = false;

        @GlobalConfig(value = "use-vanilla-random", lock = true, validator = UseVanillaRandomValidator.class)
        public boolean useVanillaRandom = false;

        private static class UseVanillaRandomValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                LeavesFeatureSet.register(LeavesFeature.of("use_vanilla_random", value));
            }
        }

        @GlobalConfig("fix-update-suppression-crash")
        public boolean updateSuppressionCrashFix = true;

        @GlobalConfig(value = "bedrock-break-list", lock = true)
        public boolean bedrockBreakList = false;

        @GlobalConfig(value = "disable-distance-check-for-use-item", validator = DisableDistanceCheckForUseItemValidator.class)
        public boolean disableDistanceCheckForUseItem = false;

        private static class DisableDistanceCheckForUseItemValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                if (!value && old != null && LeavesConfig.protocol.alternativeBlockPlacement != ProtocolConfig.AlternativePlaceType.NONE) {
                    throw new IllegalArgumentException("alternative-block-placement is enable, disable-distance-check-for-use-item always need true");
                }
            }
        }

        @GlobalConfig("no-feather-falling-trample")
        public boolean noFeatherFallingTrample = false;

        @GlobalConfig("shared-villager-discounts")
        public boolean sharedVillagerDiscounts = false;

        @GlobalConfig("disable-check-out-of-order-command")
        public boolean disableCheckOutOfOrderCommand = false;

        @GlobalConfig("despawn-enderman-with-block")
        public boolean despawnEndermanWithBlock = false;

        @GlobalConfig(value = "creative-no-clip", validator = CreativeNoClipValidator.class)
        public boolean creativeNoClip = false;

        private static class CreativeNoClipValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                CarpetRules.register(CarpetRule.of("carpet", "creativeNoClip", value));
            }
        }

        @GlobalConfig("shave-snow-layers")
        public boolean shaveSnowLayers = true;

        @GlobalConfig("ignore-lc")
        public boolean ignoreLC = false;

        @GlobalConfig("disable-packet-limit")
        public boolean disablePacketLimit = false;

        @GlobalConfig(value = "lava-riptide", validator = LavaRiptideValidator.class)
        public boolean lavaRiptide = false;

        private static class LavaRiptideValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                LeavesFeatureSet.register(LeavesFeature.of("lava_riptide", value));
            }
        }

        @GlobalConfig(value = "no-block-update-command", validator = NoBlockUpdateValidator.class)
        public boolean noBlockUpdateCommand = false;

        private static class NoBlockUpdateValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                if (value) {
                    registerCommand("blockupdate", new org.leavesmc.leaves.command.NoBlockUpdateCommand("blockupdate"));
                } else {
                    unregisterCommand("blockupdate");
                }
            }
        }

        @GlobalConfig("no-tnt-place-update")
        public boolean noTNTPlaceUpdate = false;

        @GlobalConfig("raider-die-skip-self-raid-check")
        public boolean skipSelfRaidCheck = false;

        @GlobalConfig("container-passthrough")
        public boolean containerPassthrough = false;

        @GlobalConfig(value = "avoid-anvil-too-expensive", validator = AnvilNotExpensiveValidator.class)
        public boolean avoidAnvilTooExpensive = false;

        private static class AnvilNotExpensiveValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                CarpetRules.register(CarpetRule.of("pca", "avoidAnvilTooExpensive", value));
            }
        }

        @GlobalConfig("bow-infinity-fix")
        public boolean bowInfinityFix = false;

        @GlobalConfig("hopper-counter")
        public boolean hopperCounter = false;

        @GlobalConfig(value = "spider-jockeys-drop-gapples", validator = JockeysDropGAppleValidator.class)
        public double spiderJockeysDropGapples = -1.0;

        private static class JockeysDropGAppleValidator extends DoubleConfigValidator {
            @Override
            public void verify(Double old, Double value) throws IllegalArgumentException {
                if (value > 1.0) {
                    throw new IllegalArgumentException("spider-jockeys-drop-gapples need <= 1.0f");
                }
            }
        }

        @GlobalConfig("renewable-deepslate")
        public boolean renewableDeepslate = false;

        @GlobalConfig("renewable-sponges")
        public boolean renewableSponges = false;

        @GlobalConfig(value = "renewable-coral", validator = RenewableCoralValidator.class)
        public RenewableCoralType renewableCoral = RenewableCoralType.FALSE;

        public enum RenewableCoralType {
            FALSE, TRUE, EXPANDED
        }

        private static class RenewableCoralValidator extends EnumConfigValidator<RenewableCoralType> {
            @Override
            public void verify(RenewableCoralType old, RenewableCoralType value) throws IllegalArgumentException {
                CarpetRules.register(CarpetRule.of("carpet", "renewableCoral", value));
            }
        }

        @GlobalConfig("fast-resume")
        public boolean fastResume = false;

        @GlobalConfig(value = "force-peaceful-mode", validator = ForcePeacefulModeValidator.class)
        public int forcePeacefulMode = -1;

        private static class ForcePeacefulModeValidator extends IntConfigValidator {
            @Override
            public void verify(Integer old, Integer value) throws IllegalArgumentException {
                for (ServerLevel level : MinecraftServer.getServer().getAllLevels()) {
                    level.chunkSource.peacefulModeSwitchTick = value;
                }
            }
        }

        @GlobalConfig("disable-vault-blacklist")
        public boolean disableVaultBlacklist = false;

        @RemovedConfig(name = "tick-command", category = "modify")
        @RemovedConfig(name = "player-can-edit-sign", category = "modify")
        @RemovedConfig(name = "mending-compatibility-infinity", category = {"modify", "minecraft-old"})
        @RemovedConfig(name = "protection-stacking", category = {"modify", "minecraft-old"})
        @RemovedConfig(name = "disable-moved-wrongly-threshold", category = {"modify"})
        private final boolean removed = false;
    }

    public static PerformanceConfig performance = new PerformanceConfig();

    @GlobalConfigCategory("performance")
    public static class PerformanceConfig {

        public PerformanceRemoveConfig remove = new PerformanceRemoveConfig();

        @GlobalConfigCategory("remove")
        public static class PerformanceRemoveConfig {
            @GlobalConfig("tick-guard-lambda")
            public boolean tickGuardLambda = true;

            @GlobalConfig("damage-lambda")
            public boolean damageLambda = true;
        }

        @GlobalConfig("optimized-dragon-respawn")
        public boolean optimizedDragonRespawn = false;

        @GlobalConfig("dont-send-useless-entity-packets")
        public boolean dontSendUselessEntityPackets = true;

        @GlobalConfig("enable-suffocation-optimization")
        public boolean enableSuffocationOptimization = true;

        @GlobalConfig("check-spooky-season-once-an-hour")
        public boolean checkSpookySeasonOnceAnHour = true;

        @GlobalConfig("inactive-goal-selector-disable")
        public boolean throttleInactiveGoalSelectorTick = false;

        @GlobalConfig("reduce-entity-allocations")
        public boolean reduceEntityAllocations = true;

        @GlobalConfig("cache-climb-check")
        public boolean cacheClimbCheck = true;

        @GlobalConfig(value = "biome-temperatures-use-aging-cache", lock = true)
        public boolean biomeTemperaturesUseAgingCache = true;

        @GlobalConfig("reduce-chuck-load-and-lookup")
        public boolean reduceChuckLoadAndLookup = true;

        @GlobalConfig("cache-ignite-odds")
        public boolean cacheIgniteOdds = true;

        @GlobalConfig("faster-chunk-serialization")
        public boolean fasterChunkSerialization = true;

        @GlobalConfig("skip-secondary-POI-sensor-if-absent")
        public boolean skipSecondaryPOISensorIfAbsent = true;

        @GlobalConfig("store-mob-counts-in-array")
        public boolean storeMobCountsInArray = true;

        @GlobalConfig("optimize-noise-generation")
        public boolean optimizeNoiseGeneration = false;

        @GlobalConfig("optimize-sun-burn-tick")
        public boolean optimizeSunBurnTick = true;

        @GlobalConfig("optimized-CubePointRange")
        public boolean optimizedCubePointRange = true;

        @GlobalConfig("check-frozen-ticks-before-landing-block")
        public boolean checkFrozenTicksBeforeLandingBlock = true;

        @GlobalConfig("skip-entity-move-if-movement-is-zero")
        public boolean skipEntityMoveIfMovementIsZero = true;

        @GlobalConfig("skip-cloning-advancement-criteria")
        public boolean skipCloningAdvancementCriteria = false;

        @GlobalConfig("skip-negligible-planar-movement-multiplication")
        public boolean skipNegligiblePlanarMovementMultiplication = true;

        @GlobalConfig("fix-villagers-dont-release-memory")
        public boolean villagersDontReleaseMemoryFix = false;

        @RemovedConfig(name = "cache-world-generator-sea-level", category = "performance")
        @RemovedConfig(name = "cache-ominous-banner-item", category = "performance")
        @RemovedConfig(name = "use-optimized-collection", category = "performance")
        @RemovedConfig(name = "async-pathfinding", category = "performance")
        @RemovedConfig(name = "async-mob-spawning", category = "performance")
        @RemovedConfig(name = "async-entity-tracker", category = "performance")
        @RemovedConfig(name = "fix-paper-6045", category = {"performance", "fix"})
        @RemovedConfig(name = "fix-paper-9372", category = {"performance", "fix"})
        @RemovedConfig(name = "skip-clone-loot-parameters", category = "performance")
        @RemovedConfig(name = "skip-poi-find-in-vehicle", category = "performance")
        @RemovedConfig(name = "strip-raytracing-for-entity", category = "performance")
        @RemovedConfig(name = "get-nearby-players-streams", category = {"performance", "remove"})
        @RemovedConfig(name = "optimize-world-generation-and-block-access", category = "performance")
        @RemovedConfig(name = "cache-CubeVoxelShape-shape-array", category = "performance")
        @RemovedConfig(name = "reduce-entity-fluid-lookup", category = "performance")
        @RemovedConfig(name = "optimize-entity-coordinate-key", category = "performance")
        @RemovedConfig(name = "entity-target-find-optimization", category = "performance")
        @RemovedConfig(name = "use-more-thread-unsafe-random", category = "performance")
        @RemovedConfig(name = "range-check-streams-and-iterators", category = {"performance", "remove"})
        @RemovedConfig(name = "improve-fluid-direction-caching", category = "performance")
        @RemovedConfig(name = "cache-BlockStatePairKey-hash", category = "performance")
        @RemovedConfig(name = "optimize-chunk-ticking", category = "performance")
        @RemovedConfig(name = "inventory-contains-iterators", category = {"performance", "remove"})
        private final boolean removedPerformance = true;
    }

    public static ProtocolConfig protocol = new ProtocolConfig();

    @GlobalConfigCategory("protocol")
    public static class ProtocolConfig {

        public BladerenConfig bladeren = new BladerenConfig();

        @GlobalConfigCategory("bladeren")
        public static class BladerenConfig {
            @GlobalConfig("protocol")
            public boolean enable = true;

            @GlobalConfig(value = "mspt-sync-protocol", validator = MSPTSyncValidator.class)
            public boolean msptSyncProtocol = false;

            private static class MSPTSyncValidator extends BooleanConfigValidator {
                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    LeavesFeatureSet.register(LeavesFeature.of("mspt_sync", value));
                }
            }

            @GlobalConfig(value = "mspt-sync-tick-interval", validator = MSPTSyncIntervalValidator.class)
            public int msptSyncTickInterval = 20;

            private static class MSPTSyncIntervalValidator extends IntConfigValidator {
                @Override
                public void verify(Integer old, Integer value) throws IllegalArgumentException {
                    if (value <= 0) {
                        throw new IllegalArgumentException("mspt-sync-tick-interval need > 0");
                    }
                }
            }
        }

        public SyncmaticaConfig syncmatica = new SyncmaticaConfig();

        @GlobalConfigCategory("syncmatica")
        public static class SyncmaticaConfig {
            @GlobalConfig(value = "enable", validator = SyncmaticaValidator.class)
            public boolean enable = false;

            public static class SyncmaticaValidator extends BooleanConfigValidator {
                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    if (value) {
                        org.leavesmc.leaves.protocol.syncmatica.SyncmaticaProtocol.init();
                    }
                }
            }

            @GlobalConfig("quota")
            public boolean useQuota = false;

            @GlobalConfig(value = "quota-limit")
            public int quotaLimit = 40000000;
        }

        public PCAConfig pca = new PCAConfig();

        @GlobalConfigCategory("pca")
        public static class PCAConfig {
            @RemovedConfig(name = "pca-sync-protocol", category = "protocol", transform = true)
            @GlobalConfig(value = "pca-sync-protocol", validator = PcaValidator.class)
            public boolean enable = false;

            public static class PcaValidator extends BooleanConfigValidator {
                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    if (old != null && old != value) {
                        org.leavesmc.leaves.protocol.PcaSyncProtocol.onConfigModify(value);
                    }
                }
            }

            @RemovedConfig(name = "pca-sync-player-entity", category = "protocol", transform = true)
            @GlobalConfig(value = "pca-sync-player-entity")
            public PcaPlayerEntityType syncPlayerEntity = PcaPlayerEntityType.OPS;

            public enum PcaPlayerEntityType {
                NOBODY, BOT, OPS, OPS_AND_SELF, EVERYONE
            }
        }

        public AppleSkinConfig appleskin = new AppleSkinConfig();

        @GlobalConfigCategory("appleskin")
        public static class AppleSkinConfig {
            @RemovedConfig(name = "appleskin-protocol", category = "protocol")
            @GlobalConfig("protocol")
            public boolean enable = false;

            @GlobalConfig("sync-tick-interval")
            public int syncTickInterval = 20;
        }

        public ServuxConfig servux = new ServuxConfig();

        @GlobalConfigCategory("servux")
        public static class ServuxConfig {
            @RemovedConfig(name = "servux-protocol", category = "protocol", transform = true)
            @GlobalConfig("structure-protocol")
            public boolean structureProtocol = false;

            @GlobalConfig("entity-protocol")
            public boolean entityProtocol = false;

            @GlobalConfig("hud-metadata-protocol")
            public boolean hudMetadataProtocol = false;

            @GlobalConfig("hud-metadata-protocol-share-seed")
            public boolean hudMetadataShareSeed = true;
        }

        @GlobalConfig("bbor-protocol")
        public boolean bborProtocol = false;

        @GlobalConfig("jade-protocol")
        public boolean jadeProtocol = false;

        @GlobalConfig(value = "alternative-block-placement", validator = AlternativePlaceValidator.class)
        public AlternativePlaceType alternativeBlockPlacement = AlternativePlaceType.NONE;

        public enum AlternativePlaceType {
            NONE, CARPET, CARPET_FIX, LITEMATICA
        }

        private static class AlternativePlaceValidator extends EnumConfigValidator<AlternativePlaceType> {
            @Override
            public void runAfterLoader(AlternativePlaceType value, boolean reload) {
                if (value != AlternativePlaceType.NONE) {
                    LeavesConfig.modify.disableDistanceCheckForUseItem = true;
                }
            }
        }

        @GlobalConfig("xaero-map-protocol")
        public boolean xaeroMapProtocol = false;

        @GlobalConfig(value = "xaero-map-server-id")
        public int xaeroMapServerID = new Random().nextInt();

        @GlobalConfig("leaves-carpet-support")
        public boolean leavesCarpetSupport = false;

        @GlobalConfig("lms-paster-protocol")
        public boolean lmsPasterProtocol = false;

        @GlobalConfig("rei-server-protocol")
        public boolean reiServerProtocol = false;
    }

    public static MiscConfig mics = new MiscConfig();

    @GlobalConfigCategory("misc")
    public static class MiscConfig {

        public AutoUpdateConfig autoUpdate = new AutoUpdateConfig();

        @GlobalConfigCategory("auto-update")
        public static class AutoUpdateConfig {
            @GlobalConfig(value = "enable", lock = true, validator = AutoUpdateValidator.class)
            public boolean enable = false;

            private static class AutoUpdateValidator extends BooleanConfigValidator {
                @Override
                public void runAfterLoader(Boolean value, boolean reload) {
                    if (reload) {
                        org.leavesmc.leaves.util.LeavesUpdateHelper.init();
                        if (value) {
                            LeavesLogger.LOGGER.warning("Auto-Update is not completely safe. Enabling it may cause data security problems!");
                        }
                    }
                }
            }

            @GlobalConfig(value = "download-source", lock = true, validator = DownloadSourceValidator.class)
            public String source = "application";

            public static class DownloadSourceValidator extends StringConfigValidator {
                private static final List<String> suggestSourceList = List.of("application", "ghproxy", "cloud");

                @Override
                public List<String> valueSuggest() {
                    return suggestSourceList;
                }
            }

            @GlobalConfig("allow-experimental")
            public Boolean allowExperimental = false;

            @GlobalConfig(value = "time", lock = true)
            public List<String> updateTime = List.of("14:00", "2:00");
        }

        public ExtraYggdrasilConfig yggdrasil = new ExtraYggdrasilConfig();

        @GlobalConfigCategory("extra-yggdrasil-service")
        public static class ExtraYggdrasilConfig {
            @GlobalConfig(value = "enable", validator = ExtraYggdrasilValidator.class)
            public boolean enable = false;

            public static class ExtraYggdrasilValidator extends BooleanConfigValidator {
                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    if (value) {
                        LeavesLogger.LOGGER.warning("extra-yggdrasil-service is an unofficial support. Enabling it may cause data security problems!");
                        GlobalConfiguration.get().unsupportedSettings.performUsernameValidation = true; // always check username
                    }
                }
            }

            @GlobalConfig("login-protect")
            public boolean loginProtect = false;

            @GlobalConfig(value = "urls", lock = true, validator = ExtraYggdrasilUrlsValidator.class)
            public List<String> serviceList = List.of("https://url.with.authlib-injector-yggdrasil");

            public static class ExtraYggdrasilUrlsValidator extends ListConfigValidator.STRING {
                @Override
                public void verify(List<String> old, List<String> value) throws IllegalArgumentException {
                    org.leavesmc.leaves.profile.LeavesMinecraftSessionService.initExtraYggdrasilList(value);
                }
            }
        }

        @GlobalConfig("disable-method-profiler")
        public boolean disableMethodProfiler = true;

        @RemovedConfig(name = "no-chat-sign", category = {}, transform = true)
        @GlobalConfig("no-chat-sign")
        public boolean noChatSign = true;

        @GlobalConfig("dont-respond-ping-before-start-fully")
        public boolean dontRespondPingBeforeStart = true;

        @GlobalConfig(value = "server-lang", lock = true, validator = ServerLangValidator.class)
        public String serverLang = "en_us";

        private static class ServerLangValidator extends StringConfigValidator {
            private static final List<String> supportLang = List.of("en_us", "zh_cn");

            @Override
            public void verify(String old, String value) throws IllegalArgumentException {
                if (!supportLang.contains(value)) {
                    throw new IllegalArgumentException("lang " + value + " not supported");
                }
            }

            @Override
            public List<String> valueSuggest() {
                return supportLang;
            }
        }

        @GlobalConfig(value = "server-mod-name")
        public String serverModName = "Leaves";

        @GlobalConfig("bstats-privacy-mode")
        public boolean bstatsPrivacyMode = false;

        @GlobalConfig("force-minecraft-command")
        public boolean forceMinecraftCommand = false;

        @GlobalConfig("leaves-packet-event")
        public boolean leavesPacketEvent = true;
    }

    public static RegionConfig region = new RegionConfig();

    @GlobalConfigCategory("region")
    public static class RegionConfig {

        @GlobalConfig(value = "format", lock = true, validator = RegionFormatValidator.class)
        public org.leavesmc.leaves.region.RegionFileFormat format = org.leavesmc.leaves.region.RegionFileFormat.ANVIL;

        private static class RegionFormatValidator extends EnumConfigValidator<org.leavesmc.leaves.region.RegionFileFormat> {
            @Override
            public void verify(RegionFileFormat old, RegionFileFormat value) throws IllegalArgumentException {
                org.leavesmc.leaves.region.IRegionFileFactory.initFirstRegion(value);
            }
        }

        public LinearConfig linear = new LinearConfig();

        @GlobalConfigCategory("linear")
        public static class LinearConfig {

            @GlobalConfig(value = "version", lock = true)
            public org.leavesmc.leaves.region.linear.LinearVersion version = org.leavesmc.leaves.region.linear.LinearVersion.V2;

            @GlobalConfig(value = "auto-convert-anvil-to-linear", lock = true)
            public boolean autoConvertAnvilToLinear = false;

            @GlobalConfig(value = "flush-max-threads", lock = true)
            public int flushThreads = 6;

            public int getLinearFlushThreads() {
                if (flushThreads <= 0) {
                    return Math.max(Runtime.getRuntime().availableProcessors() + flushThreads, 1);
                } else {
                    return flushThreads;
                }
            }

            @GlobalConfig(value = "flush-delay-ms", lock = true)
            public int flushDelayMs = 100;

            @GlobalConfig(value = "use-virtual-thread", lock = true)
            public boolean useVirtualThread = true;

            @GlobalConfig(value = "compression-level", lock = true, validator = LinearCompressValidator.class)
            public int compressionLevel = 1;

            private static class LinearCompressValidator extends IntConfigValidator {
                @Override
                public void verify(Integer old, Integer value) throws IllegalArgumentException {
                    if (value < 1 || value > 22) {
                        throw new IllegalArgumentException("linear.compression-level need between 1 and 22");
                    }
                }
            }

            @RemovedConfig(name = "flush-frequency", category = {"region", "linear"})
            @RemovedConfig(name = "crash-on-broken-symlink", category = {"region", "linear"})
            private final boolean linearCrashOnBrokenSymlink = true;
        }
    }

    public static FixConfig fix = new FixConfig();

    @GlobalConfigCategory("fix")
    public static class FixConfig {
        @GlobalConfig("vanilla-hopper")
        public boolean vanillaHopper = false;

        @RemovedConfig(name = "spigot-EndPlatform-destroy", category = "fix")
        private final boolean spigotEndPlatformDestroy = false;
    }
}
