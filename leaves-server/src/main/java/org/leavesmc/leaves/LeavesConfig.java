package org.leavesmc.leaves;

import com.destroystokyo.paper.util.SneakyThrow;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.configuration.GlobalConfiguration;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.BotCommand;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.command.LeavesCommand;
import org.leavesmc.leaves.config.ConfigValidatorImpl.BooleanConfigValidator;
import org.leavesmc.leaves.config.ConfigValidatorImpl.DoubleConfigValidator;
import org.leavesmc.leaves.config.ConfigValidatorImpl.EnumConfigValidator;
import org.leavesmc.leaves.config.ConfigValidatorImpl.IntConfigValidator;
import org.leavesmc.leaves.config.ConfigValidatorImpl.ListConfigValidator;
import org.leavesmc.leaves.config.ConfigValidatorImpl.LongConfigValidator;
import org.leavesmc.leaves.config.ConfigValidatorImpl.StringConfigValidator;
import org.leavesmc.leaves.config.GlobalConfigManager;
import org.leavesmc.leaves.config.annotations.GlobalConfig;
import org.leavesmc.leaves.config.annotations.GlobalConfigCategory;
import org.leavesmc.leaves.config.annotations.RemovedConfig;
import org.leavesmc.leaves.profile.LeavesMinecraftSessionService;
import org.leavesmc.leaves.protocol.CarpetServerProtocol.CarpetRule;
import org.leavesmc.leaves.protocol.CarpetServerProtocol.CarpetRules;
import org.leavesmc.leaves.protocol.PcaSyncProtocol;
import org.leavesmc.leaves.protocol.bladeren.BladerenProtocol.LeavesFeature;
import org.leavesmc.leaves.protocol.bladeren.BladerenProtocol.LeavesFeatureSet;
import org.leavesmc.leaves.protocol.rei.REIServerProtocol;
import org.leavesmc.leaves.protocol.servux.logger.DataLogger;
import org.leavesmc.leaves.protocol.syncmatica.SyncmaticaProtocol;
import org.leavesmc.leaves.region.IRegionFileFactory;
import org.leavesmc.leaves.region.RegionFileFormat;
import org.leavesmc.leaves.region.linear.LinearVersion;
import org.leavesmc.leaves.util.LeavesUpdateHelper;
import org.leavesmc.leaves.util.MathUtils;
import org.leavesmc.leaves.util.McTechnicalModeHelper;
import org.leavesmc.leaves.util.ServerI18nUtil;
import org.leavesmc.leaves.util.VillagerInfiniteDiscountHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;

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

        registerCommand("leaves", new LeavesCommand());
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
                        registerCommand("bot", new BotCommand());
                        Actions.registerAll();
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

            @GlobalConfig("resident-fakeplayer")
            public boolean canResident = false;

            @GlobalConfig("open-fakeplayer-inventory")
            public boolean canOpenInventory = false;

            @GlobalConfig("use-action")
            public boolean canUseAction = true;

            @GlobalConfig("modify-config")
            public boolean canModifyConfig = false;

            @GlobalConfig("manual-save-and-load")
            public boolean canManualSaveAndLoad = false;

            @GlobalConfig(value = "cache-skin", lock = true)
            public boolean useSkinCache = false;

            public InGameConfig inGame = new InGameConfig();

            @GlobalConfigCategory("in-game")
            public static class InGameConfig {

                @RemovedConfig(name = "always-send-data", category = {"modify", "fakeplayer"}, transform = true)
                @GlobalConfig("always-send-data")
                public boolean canSendDataAlways = true;

                @RemovedConfig(name = "skip-sleep-check", category = {"modify", "fakeplayer"}, transform = true)
                @GlobalConfig("skip-sleep-check")
                public boolean canSkipSleep = false;

                @RemovedConfig(name = "spawn-phantom", category = {"modify", "fakeplayer"}, transform = true)
                @GlobalConfig("spawn-phantom")
                public boolean canSpawnPhantom = false;

                @RemovedConfig(name = "tick-type", category = {"modify", "fakeplayer"}, transform = true)
                @GlobalConfig("tick-type")
                public ServerBot.TickType tickType = ServerBot.TickType.NETWORK;

                @GlobalConfig(value = "simulation-distance", validator = BotSimulationDistanceValidator.class)
                private int simulationDistance = -1;

                public int getSimulationDistance(ServerBot bot) {
                    return this.simulationDistance == -1 ? bot.getBukkitEntity().getSimulationDistance() : this.simulationDistance;
                }

                public static class BotSimulationDistanceValidator extends IntConfigValidator {
                    @Override
                    public void verify(Integer old, Integer value) throws IllegalArgumentException {
                        if ((value < 2 && value != -1) || value > 32) {
                            throw new IllegalArgumentException("simulation-distance must be a number between 2 and 32, got: " + value);
                        }
                    }
                }

                @GlobalConfig("enable-locator-bar")
                public boolean enableLocatorBar = false;
            }
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

                @GlobalConfig("sound-update-suppression")
                public boolean soundUpdateSuppression = false;

                @RemovedConfig(name = "redstone-wire-dont-connect-if-on-trapdoor", category = "modify", transform = true)
                @RemovedConfig(name = "redstone-wire-dont-connect-if-on-trapdoor", category = {"modify", "minecraft-old"}, transform = true)
                @RemovedConfig(name = "redstone-wire-dont-connect-if-on-trapdoor", category = {"modify", "minecraft-old", "block-updater"}, transform = true)
                @GlobalConfig("redstone-ignore-upwards-update")
                public boolean redstoneIgnoreUpwardsUpdate = false;

                @RemovedConfig(name = "old-block-entity-behaviour", category = {"modify", "minecraft-old"}, transform = true)
                @RemovedConfig(name = "old-block-entity-behaviour", category = {"modify", "minecraft-old", "block-updater"}, transform = true)
                @GlobalConfig("old-block-remove-behaviour")
                public boolean oldBlockRemoveBehaviour = false;
            }

            @RemovedConfig(name = "shears-in-dispenser-can-zero-amount", category = {}, transform = true)
            @RemovedConfig(name = "shears-in-dispenser-can-zero-amount", category = "modify", transform = true)
            @GlobalConfig("shears-in-dispenser-can-zero-amount")
            public boolean shearsInDispenserCanZeroAmount = false;

            @GlobalConfig("armor-stand-cant-kill-by-mob-projectile")
            public boolean armorStandCantKillByMobProjectile = false;

            @GlobalConfig(value = "villager-infinite-discounts", validator = VillagerInfiniteDiscountsValidator.class)
            private boolean villagerInfiniteDiscounts = false;

            private static class VillagerInfiniteDiscountsValidator extends BooleanConfigValidator {
                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    VillagerInfiniteDiscountHelper.doVillagerInfiniteDiscount(value);
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

            @GlobalConfig("spawn-invulnerable-time")
            public boolean spawnInvulnerableTime = false;

            @GlobalConfig("old-hopper-suck-in-behavior")
            public boolean oldHopperSuckInBehavior = false;

            @GlobalConfig("old-zombie-piglin-drop")
            public boolean oldZombiePiglinDrop = false;

            public RaidConfig raid = new RaidConfig();

            @GlobalConfigCategory("revert-raid-changes")
            public static class RaidConfig {
                @GlobalConfig("allow-bad-omen-trigger-raid")
                public boolean allowBadOmenTriggerRaid = false;

                @GlobalConfig("give-bad-omen-when-kill-patrol-leader")
                public boolean giveBadOmenWhenKillPatrolLeader = false;

                @GlobalConfig("use-old-find-spawn-position")
                public boolean useOldFindSpawnPosition = false;

                @GlobalConfig("skip-height-check")
                public boolean skipHeightCheck = false;
            }

            @GlobalConfig("old-zombie-reinforcement")
            public boolean oldZombieReinforcement = false;

            @GlobalConfig("allow-anvil-destroy-item-entities")
            public boolean allowAnvilDestroyItemEntities = false;

            public TripwireConfig tripwire = new TripwireConfig();

            @GlobalConfigCategory("tripwire-and-hook-behavior")
            public static class TripwireConfig {
                @RemovedConfig(name = "string-tripwire-hook-duplicate", category = {"modify", "minecraft-old"}, transform = true)
                @GlobalConfig("string-tripwire-hook-duplicate")
                public boolean stringTripwireHookDuplicate = false;

                @GlobalConfig("tripwire-behavior")
                public TripwireBehavior tripwireBehavior = TripwireBehavior.VANILLA_21;

                public enum TripwireBehavior {
                    VANILLA_20, VANILLA_21, MIXED
                }
            }

            @GlobalConfig(value = "void-trade", validator = VoidTradeValidator.class)
            public boolean voidTrade = false;

            private static class VoidTradeValidator extends BooleanConfigValidator {
                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    if (!value && old != null && LeavesConfig.modify.forceVoidTrade) {
                        throw new IllegalArgumentException("force-void-trade is enable, void-trade always need true");
                    }
                }
            }

            @GlobalConfig("disable-item-damage-check")
            public boolean disableItemDamageCheck = false;

            @GlobalConfig("old-throwable-projectile-tick-order")
            public boolean oldThrowableProjectileTickOrder = false;

            @GlobalConfig("keep-leash-connect-when-use-firework")
            public boolean keepLeashConnectWhenUseFirework = false;

            @GlobalConfig("tnt-wet-explosion-no-item-damage")
            public boolean tntWetExplosionNoItemDamage = false;

            @GlobalConfig("old-projectile-explosion-behavior")
            public boolean oldProjectileExplosionBehavior = false;

            @GlobalConfig("ender-dragon-part-can-use-end-portal")
            public boolean enderDragonPartCanUseEndPortal = false;
        }

        public ElytraAeronauticsConfig elytraAeronautics = new ElytraAeronauticsConfig();

        @GlobalConfigCategory("elytra-aeronautics")
        public static class ElytraAeronauticsConfig {
            @GlobalConfig("no-chunk-load")
            public boolean enableNoChunkLoad = false;

            @GlobalConfig(value = "no-chunk-height")
            public double noChunkHeight = 500.0D;

            @GlobalConfig(value = "no-chunk-speed")
            public double noChunkSpeed = -1.0D;

            @GlobalConfig("message")
            public boolean doSendMessages = true;

            @GlobalConfig(value = "message-start")
            public String startMessage = "Flight enter cruise mode";

            @GlobalConfig(value = "message-end")
            public String endMessage = "Flight exit cruise mode";
        }

        @RemovedConfig(name = "redstone-shears-wrench", category = {}, transform = true)
        @GlobalConfig("redstone-shears-wrench")
        public boolean redstoneShearsWrench = true;

        @RemovedConfig(name = "budding-amethyst-can-push-by-piston", category = {}, transform = true)
        @RemovedConfig(name = "budding-amethyst-can-push-by-piston", category = "modify", transform = true)
        @GlobalConfig(value = "movable-budding-amethyst", validator = MovableBuddingAmethystValidator.class)
        public boolean movableBuddingAmethyst = false;

        private static class MovableBuddingAmethystValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                CarpetRules.register(CarpetRule.of("carpet", "movableAmethyst", value));
            }
        }

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

        public ShulkerBoxConfig shulkerBox = new ShulkerBoxConfig();

        @GlobalConfigCategory("shulker-box")
        public static class ShulkerBoxConfig {
            public int shulkerBoxStackSize = 1;
            @RemovedConfig(name = "stackable-shulker-boxes", category = "modify", transform = true)
            @GlobalConfig(value = "stackable-shulker-boxes", validator = StackableShulkerValidator.class)
            private String stackableShulkerBoxes = "false";

            private static class StackableShulkerValidator extends StringConfigValidator {
                @Override
                public void verify(String old, String value) throws IllegalArgumentException {
                    String realValue = MathUtils.isNumeric(value) ? value : value.equals("true") ? "2" : "1";
                    LeavesConfig.modify.shulkerBox.shulkerBoxStackSize = Integer.parseInt(realValue);
                }
            }

            @GlobalConfig(value = "same-nbt-stackable")
            public boolean sameNbtStackable = false;
        }

        @GlobalConfig(value = "force-void-trade", validator = ForceVoidTradeValidator.class)
        public boolean forceVoidTrade = false;

        private static class ForceVoidTradeValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                if (value) {
                    LeavesConfig.modify.oldMC.voidTrade = true;
                }
            }

            @Override
            public void runAfterLoader(Boolean value, boolean reload) {
                if (value) {
                    LeavesConfig.modify.oldMC.voidTrade = true;
                }
            }
        }

        @GlobalConfig(value = "mc-technical-survival-mode", validator = McTechnicalModeValidator.class, lock = true)
        public boolean mcTechnicalMode = true;

        private static class McTechnicalModeValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                if (value) {
                    McTechnicalModeHelper.doMcTechnicalMode();
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

        @GlobalConfig(value = "no-block-update-command")
        public boolean noBlockUpdateCommand = false;

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

        public HopperCounterConfig hopperCounter = new HopperCounterConfig();

        @GlobalConfigCategory("counter")
        public static class HopperCounterConfig {
            @RemovedConfig(name = "hopper-counter", category = "modify", transform = true)
            @GlobalConfig("enable")
            public boolean enable = false;

            @GlobalConfig("unlimited-speed")
            public boolean unlimitedSpeed = false;
        }

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

        @GlobalConfig("disable-vault-blacklist")
        public boolean disableVaultBlacklist = false;

        @GlobalConfig(value = "exp-orb-absorb-mode", validator = ExpOrbModeValidator.class)
        private ExpOrbAbsorbMode expOrbAbsorbMode = ExpOrbAbsorbMode.VANILLA;

        public Predicate<ServerPlayer> fastAbsorbPredicate = player -> false;

        public enum ExpOrbAbsorbMode {
            VANILLA, FAST, FAST_CREATIVE
        }

        private static class ExpOrbModeValidator extends EnumConfigValidator<ExpOrbAbsorbMode> {
            @Override
            public void verify(ExpOrbAbsorbMode old, ExpOrbAbsorbMode value) throws IllegalArgumentException {
                LeavesConfig.modify.fastAbsorbPredicate = switch (value) {
                    case FAST -> player -> true;
                    case VANILLA -> player -> false;
                    case FAST_CREATIVE -> Player::hasInfiniteMaterials;
                };
            }
        }

        @RemovedConfig(name = "tick", category = {"modify", "force-peaceful-mode-switch"})
        @RemovedConfig(name = "types", category = {"modify", "force-peaceful-mode-switch"})
        @RemovedConfig(name = "force-peaceful-mode-switch", category = "modify")
        @RemovedConfig(name = "force-peaceful-mode", category = "modify")
        @RemovedConfig(name = "tick-command", category = "modify")
        @RemovedConfig(name = "player-can-edit-sign", category = "modify")
        @RemovedConfig(name = "mending-compatibility-infinity", category = {"modify", "minecraft-old"})
        @RemovedConfig(name = "protection-stacking", category = {"modify", "minecraft-old"})
        @RemovedConfig(name = "disable-moved-wrongly-threshold", category = "modify")
        @RemovedConfig(name = "ignore-lc", category = "modify")
        @RemovedConfig(name = "fix-fortress-mob-spawn", category = {"modify", "minecraft-old"})
        @RemovedConfig(name = "fast-resume", category = "modify")
        @RemovedConfig(name = "old-nether-portal-collision", category = {"modify", "minecraft-old"})
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

        @GlobalConfig(value = "sleeping-block-entity", lock = true)
        public boolean sleepingBlockEntity = false;

        @RemovedConfig(name = "biome-temperatures-use-aging-cache", category = "performance")
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
                    SyncmaticaProtocol.init(value);
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
                        PcaSyncProtocol.onConfigModify(value);
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

            @GlobalConfig("hud-logger-protocol")
            public boolean hudLoggerProtocol = false;

            @GlobalConfig("hud-enabled-loggers")
            public List<DataLogger.Type> hudEnabledLoggers = List.of(DataLogger.Type.TPS, DataLogger.Type.MOB_CAPS);

            @GlobalConfig("hud-update-interval")
            public int hudUpdateInterval = 1;

            @GlobalConfig("hud-metadata-protocol-share-seed")
            public boolean hudMetadataShareSeed = true;

            public LitematicsConfig litematics = new LitematicsConfig();

            @GlobalConfigCategory("litematics")
            public static class LitematicsConfig {

                @RemovedConfig(name = "litematics-protocol", category = {"protocol", "servux"}, transform = true)
                @GlobalConfig(value = "enable", validator = LitematicsProtocolValidator.class)
                public boolean enable = false;

                @GlobalConfig(value = "max-nbt-size", validator = MaxNbtSizeValidator.class)
                public long maxNbtSize = 2097152;

                public static class MaxNbtSizeValidator extends LongConfigValidator {

                    @Override
                    public void verify(Long old, Long value) throws IllegalArgumentException {
                        if (value <= 0) {
                            throw new IllegalArgumentException("Max nbt size can not be <= 0");
                        }
                    }
                }

                public static class LitematicsProtocolValidator extends BooleanConfigValidator {
                    @Override
                    public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                        PluginManager pluginManager = MinecraftServer.getServer().server.getPluginManager();
                        if (value) {
                            if (pluginManager.getPermission("leaves.protocol.litematics") == null) {
                                pluginManager.addPermission(new Permission("leaves.protocol.litematics", PermissionDefault.OP));
                            }
                        } else {
                            pluginManager.removePermission("leaves.protocol.litematics");
                        }
                    }
                }
            }
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
            public void verify(AlternativePlaceType old, AlternativePlaceType value) throws IllegalArgumentException {
                if (value != AlternativePlaceType.NONE) {
                    LeavesConfig.modify.disableDistanceCheckForUseItem = true;
                }
            }

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

        @GlobalConfig(value = "rei-server-protocol", validator = ReiValidator.class)
        public boolean reiServerProtocol = false;

        public static class ReiValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                if (old != value && value != null) {
                    REIServerProtocol.onConfigModify(value);
                }
            }
        }

        @GlobalConfig("chat-image-protocol")
        public boolean chatImageProtocol = false;

        @RemovedConfig(name = "recipe-send-all", category = {"protocol"})
        public boolean recipeSendAll = false;

        @RemovedConfig(name = "lms-paster-protocol", category = {"protocol"})
        public boolean lmsPasterProtocol = false;
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
                    if (!reload) {
                        LeavesUpdateHelper.init();
                        if (value) {
                            LeavesLogger.LOGGER.warning("Auto-Update is not completely safe. Enabling it may cause data security problems!");
                        }
                    }
                }
            }

            @GlobalConfig(value = "download-source", lock = true, validator = DownloadSourceValidator.class)
            public String source = "application";

            public static class DownloadSourceValidator extends StringConfigValidator {
                private static final List<String> suggestSourceList = List.of("application", "cloud");

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
            private List<String> serviceList = List.of("https://url.with.authlib-injector-yggdrasil");

            public static class ExtraYggdrasilUrlsValidator extends ListConfigValidator.STRING {
                @Override
                public void verify(List<String> old, List<String> value) throws IllegalArgumentException {
                    LeavesMinecraftSessionService.initExtraYggdrasilList(value);
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
            private static final List<String> supportLang = new ArrayList<>(List.of("en_us"));

            @Override
            public void verify(String old, String value) throws IllegalArgumentException {
                if (!ServerI18nUtil.finishPreload ||
                    !ServerI18nUtil.tryAppendLanguages(supportLang)) {
                    return;
                }
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

        @GlobalConfig("chat-command-max-length")
        public int chatCommandMaxLength = 32767;
    }

    public static RegionConfig region = new RegionConfig();

    @GlobalConfigCategory("region")
    public static class RegionConfig {

        @GlobalConfig(value = "format", lock = true, validator = RegionFormatValidator.class)
        public RegionFileFormat format = RegionFileFormat.ANVIL;

        private static class RegionFormatValidator extends EnumConfigValidator<RegionFileFormat> {
            @Override
            public void verify(RegionFileFormat old, RegionFileFormat value) throws IllegalArgumentException {
                IRegionFileFactory.initFirstRegion(value);
            }
        }

        public LinearConfig linear = new LinearConfig();

        @GlobalConfigCategory("linear")
        public static class LinearConfig {

            @GlobalConfig(value = "version", lock = true)
            public LinearVersion version = LinearVersion.V2;

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
            @RemovedConfig(name = "auto-convert-anvil-to-linear", category = {"region", "linear"})
            private final boolean removed = true;
        }
    }

    public static FixConfig fix = new FixConfig();

    @GlobalConfigCategory("fix")
    public static class FixConfig {
        @GlobalConfig("vanilla-hopper")
        public boolean vanillaHopper = false;

        @GlobalConfig(value = "vanilla-display-name", validator = DisplayNameValidator.class)
        public boolean vanillaDisplayName = true;

        private static class DisplayNameValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                if (value == old) {
                    return;
                }
                for (ServerPlayer player : MinecraftServer.getServer().getPlayerList().getPlayers()) {
                    player.adventure$displayName = value ? PaperAdventure.asAdventure(player.getDisplayName()) : Component.text(player.getScoreboardName());
                }
            }
        }

        @GlobalConfig("vanilla-portal-handle")
        public boolean vanillaPortalHandle = false;

        @GlobalConfig("vanilla-fluid-pushing")
        public boolean vanillaFluidPushing = true;

        @GlobalConfig(value = "collision-behavior")
        public CollisionBehavior collisionBehavior = CollisionBehavior.BLOCK_SHAPE_VANILLA;

        public enum CollisionBehavior {
            VANILLA, BLOCK_SHAPE_VANILLA, PAPER
        }

        @RemovedConfig(name = "spigot-EndPlatform-destroy", category = "fix")
        @RemovedConfig(name = "vanilla-endermite-spawn", category = "fix")
        private final boolean removed = false;
    }
}
