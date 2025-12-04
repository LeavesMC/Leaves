package org.leavesmc.leaves;

import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.configuration.GlobalConfiguration;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.bot.BotCommand;
import org.leavesmc.leaves.command.leaves.LeavesCommand;
import org.leavesmc.leaves.config.GlobalConfigManager;
import org.leavesmc.leaves.config.annotations.GlobalConfig;
import org.leavesmc.leaves.config.annotations.GlobalConfigCategory;
import org.leavesmc.leaves.config.annotations.TransferConfig;
import org.leavesmc.leaves.config.api.ConfigTransformer;
import org.leavesmc.leaves.config.api.ConfigValidator;
import org.leavesmc.leaves.config.api.impl.ConfigValidatorImpl.BooleanConfigValidator;
import org.leavesmc.leaves.config.api.impl.ConfigValidatorImpl.DoubleConfigValidator;
import org.leavesmc.leaves.config.api.impl.ConfigValidatorImpl.EnumConfigValidator;
import org.leavesmc.leaves.config.api.impl.ConfigValidatorImpl.IntConfigValidator;
import org.leavesmc.leaves.config.api.impl.ConfigValidatorImpl.ListConfigValidator;
import org.leavesmc.leaves.config.api.impl.ConfigValidatorImpl.LongConfigValidator;
import org.leavesmc.leaves.config.api.impl.ConfigValidatorImpl.StringConfigValidator;
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
import java.util.Random;
import java.util.function.Predicate;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
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
                throw new RuntimeException(ex);
            }
        }

        LeavesConfig.config.set("config-version", CURRENT_CONFIG_VERSION);

        GlobalConfigManager.init();

        LeavesCommand.INSTANCE.register();
    }

    public static void reload() {
        if (!LeavesConfig.configFile.exists()) {
            throw new RuntimeException("Leaves config file not found, please restart the server");
        }

        try {
            config.load(LeavesConfig.configFile);
        } catch (final Exception ex) {
            LeavesLogger.LOGGER.severe("Failure to reload leaves config", ex);
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

    public static ModifyConfig modify = new ModifyConfig();

    @GlobalConfigCategory("modify")
    public static class ModifyConfig {

        public FakeplayerConfig fakeplayer = new FakeplayerConfig();

        @GlobalConfigCategory("fakeplayer")
        public static class FakeplayerConfig {

            @TransferConfig("fakeplayer.enable")
            @GlobalConfig(value = "enable", validator = FakeplayerValidator.class)
            public boolean enable = true;

            private static class FakeplayerValidator extends BooleanConfigValidator {
                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    if (!value.equals(old)) {
                        if (value) {
                            BotCommand.INSTANCE.register();
                        } else {
                            BotCommand.INSTANCE.unregister();
                            BotList.INSTANCE.removeAll();
                        }
                    }
                }
            }

            @TransferConfig("fakeplayer.unable-fakeplayer-names")
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

            @GlobalConfig(value = "use-action", validator = CanUseConfigValidator.class)
            public boolean canUseAction = true;

            private static class CanUseConfigValidator extends BotSubcommandValidator {
                private CanUseConfigValidator() {
                    super("use");
                }
            }

            @GlobalConfig(value = "modify-config", validator = CanModifyConfigValidator.class)
            public boolean canModifyConfig = false;

            private static class CanModifyConfigValidator extends BotSubcommandValidator {
                private CanModifyConfigValidator() {
                    super("config");
                }
            }

            @GlobalConfig(value = "manual-save-and-load", validator = CanManualSaveAndLoadValidator.class)
            public boolean canManualSaveAndLoad = false;

            private static class CanManualSaveAndLoadValidator extends BotSubcommandValidator {
                private CanManualSaveAndLoadValidator() {
                    super("save", "load");
                }
            }

            private static class BotSubcommandValidator extends BooleanConfigValidator {
                private final List<String> subcommands;

                private BotSubcommandValidator(String... subcommand) {
                    this.subcommands = List.of(subcommand);
                }

                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    if (old != null && !old.equals(value)) {
                        Bukkit.getOnlinePlayers().stream()
                            .filter(sender -> subcommands.stream().allMatch(subcommand -> BotCommand.hasPermission(sender, subcommand)))
                            .forEach(org.bukkit.entity.Player::updateCommands);
                    }
                }
            }

            @GlobalConfig(value = "cache-skin", lock = true)
            public boolean useSkinCache = false;

            public InGameConfig inGame = new InGameConfig();

            @GlobalConfigCategory("in-game")
            public static class InGameConfig {

                @TransferConfig("modify.fakeplayer.always-send-data")
                @GlobalConfig("always-send-data")
                public boolean canSendDataAlways = true;

                @TransferConfig("modify.fakeplayer.skip-sleep-check")
                @GlobalConfig("skip-sleep-check")
                public boolean canSkipSleep = false;

                @TransferConfig("modify.fakeplayer.spawn-phantom")
                @GlobalConfig("spawn-phantom")
                public boolean canSpawnPhantom = false;

                @TransferConfig("modify.fakeplayer.tick-type")
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
                @TransferConfig("modify.instant-block-updater-reintroduced")
                @TransferConfig("modify.minecraft-old.instant-block-updater-reintroduced")
                @GlobalConfig(value = "instant-block-updater-reintroduced", lock = true)
                public boolean instantBlockUpdaterReintroduced = false;

                @TransferConfig("modify.minecraft-old.cce-update-suppression")
                @GlobalConfig("cce-update-suppression")
                public boolean cceUpdateSuppression = false;

                @GlobalConfig("sound-update-suppression")
                public boolean soundUpdateSuppression = false;

                @TransferConfig("modify.redstone-wire-dont-connect-if-on-trapdoor")
                @TransferConfig("modify.minecraft-old.redstone-wire-dont-connect-if-on-trapdoor")
                @TransferConfig("modify.minecraft-old.block-updater.redstone-wire-dont-connect-if-on-trapdoor")
                @GlobalConfig("redstone-ignore-upwards-update")
                public boolean redstoneIgnoreUpwardsUpdate = false;

                @TransferConfig("modify.minecraft-old.old-block-entity-behaviour")
                @TransferConfig("modify.minecraft-old.block-updater.old-block-entity-behaviour")
                @GlobalConfig("old-block-remove-behaviour")
                public boolean oldBlockRemoveBehaviour = false;
            }

            @TransferConfig("shears-in-dispenser-can-zero-amount")
            @TransferConfig("modify.shears-in-dispenser-can-zero-amount")
            @GlobalConfig("shears-in-dispenser-can-zero-amount")
            public boolean shearsInDispenserCanZeroAmount = false;

            @SuppressWarnings("unused")
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

            @TransferConfig("modify.zero-tick-plants")
            @GlobalConfig("zero-tick-plants")
            public boolean zeroTickPlants = false;

            @TransferConfig("modify.minecraft-old.loot-world-random")
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

            @TransferConfig(value = "modify.minecraft-old.revert-raid-changes", transformer = RaidConfigTransformer.class)
            @GlobalConfig("old-raid-behavior")
            public boolean oldRaidBehavior = false;

            public static class RaidConfigTransformer implements ConfigTransformer<MemorySection, Boolean> {
                @Override
                public Boolean transform(@NotNull MemorySection raidConfig) {
                    return raidConfig.getBoolean("allow-bad-omen-trigger-raid")
                        || raidConfig.getBoolean("give-bad-omen-when-kill-patrol-leader")
                        || raidConfig.getBoolean("skip-height-check")
                        || raidConfig.getBoolean("use-old-find-spawn-position");
                }
            }

            @GlobalConfig("old-zombie-reinforcement")
            public boolean oldZombieReinforcement = false;

            @GlobalConfig("allow-anvil-destroy-item-entities")
            public boolean allowAnvilDestroyItemEntities = false;

            public TripwireConfig tripwire = new TripwireConfig();

            @GlobalConfigCategory("tripwire-and-hook-behavior")
            public static class TripwireConfig {
                @TransferConfig("modify.minecraft-old.string-tripwire-hook-duplicate")
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

            @GlobalConfig("old-minecart-motion-behavior")
            public boolean oldMinecartMotionBehavior = false;
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

        @TransferConfig("redstone-shears-wrench")
        @GlobalConfig("redstone-shears-wrench")
        public boolean redstoneShearsWrench = false;

        @TransferConfig("budding-amethyst-can-push-by-piston")
        @TransferConfig("modify.budding-amethyst-can-push-by-piston")
        @GlobalConfig(value = "movable-budding-amethyst", validator = MovableBuddingAmethystValidator.class)
        public boolean movableBuddingAmethyst = false;

        private static class MovableBuddingAmethystValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                CarpetRules.register(CarpetRule.of("carpet", "movableAmethyst", value));
            }
        }

        @TransferConfig("spectator-dont-get-advancement")
        @GlobalConfig("spectator-dont-get-advancement")
        public boolean spectatorDontGetAdvancement = false;

        @TransferConfig("stick-change-armorstand-arm-status")
        @GlobalConfig("stick-change-armorstand-arm-status")
        public boolean stickChangeArmorStandArmStatus = true;

        @TransferConfig("snowball-and-egg-can-knockback-player")
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

            @TransferConfig("modify.stackable-shulker-boxes")
            @GlobalConfig(value = "stackable-shulker-boxes", validator = StackableShulkerValidator.class)
            public int stackableShulkerBoxes = 1;

            private static class StackableShulkerValidator implements ConfigValidator<Integer> {

                @Override
                public Integer loadConvert(Object value) throws IllegalArgumentException {
                    switch (value) {
                        case String stringValue -> {
                            if (stringValue.equals("true")) {
                                return 2;
                            } else if (!MathUtils.isNumeric(stringValue)) {
                                return 1;
                            } else {
                                return Integer.parseInt(stringValue);
                            }
                        }
                        case Integer integerValue -> {
                            return integerValue;
                        }
                        case Boolean boolValue -> {
                            return boolValue ? 2 : 1;
                        }
                        case null, default -> throw new IllegalArgumentException("stackable-shulker-boxes need string or integer or boolean");
                    }
                }

                @Override
                public Object saveConvert(Integer value) {
                    if (value == 1) {
                        return false;
                    } else if (value == 2) {
                        return true;
                    } else {
                        return value;
                    }
                }

                @Override
                public Integer stringConvert(String value) throws IllegalArgumentException {
                    return loadConvert(value);
                }

                @Override
                public void verify(Integer old, Integer value) throws IllegalArgumentException {
                    if (value < 1 || value > 64) {
                        throw new IllegalArgumentException("stackable-shulker-boxes need >= 1 and <= 64");
                    }
                }

                @Override
                public List<String> valueSuggest() {
                    return List.of("true", "false", "64", "32");
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

        @GlobalConfig(value = "no-block-update-command", validator = NoBlockUpdateValidator.class)
        public boolean noBlockUpdateCommand = false;

        private static class NoBlockUpdateValidator extends BooleanConfigValidator {
            @Override
            public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                if (old != null && !old.equals(value)) {
                    Bukkit.getOnlinePlayers().stream()
                        .filter(sender -> LeavesCommand.hasPermission(sender, "blockupdate"))
                        .forEach(org.bukkit.entity.Player::updateCommands);
                }
            }
        }

        @GlobalConfig("no-tnt-place-update")
        public boolean noTNTPlaceUpdate = false;

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

        @GlobalConfigCategory("hopper-counter")
        public static class HopperCounterConfig {
            @TransferConfig(value = "modify.hopper-counter", transformer = HopperCounterTransfer.class)
            @TransferConfig("modify.counter.enable")
            @GlobalConfig(value = "enable", validator = HopperCounterValidator.class)
            public boolean enable = false;

            private static class HopperCounterValidator extends BooleanConfigValidator {
                @Override
                public void verify(Boolean old, Boolean value) throws IllegalArgumentException {
                    if (old != null && !old.equals(value)) {
                        Bukkit.getOnlinePlayers().stream()
                            .filter(sender -> LeavesCommand.hasPermission(sender, "counter"))
                            .forEach(org.bukkit.entity.Player::updateCommands);
                    }
                }
            }

            @TransferConfig("modify.counter.unlimited-speed")
            @GlobalConfig("unlimited-speed")
            public boolean unlimitedSpeed = false;

            private static class HopperCounterTransfer implements ConfigTransformer<Object, Boolean> {
                @Override
                public Boolean transform(Object object) throws StopTransformException {
                    if (object instanceof Boolean) {
                        return (Boolean) object;
                    } else {
                        throw new StopTransformException();
                    }
                }
            }
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

        @SuppressWarnings("unused")
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

        @GlobalConfig("follow-tick-sequence-merge")
        public boolean followTickSequenceMerge = false;
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

        @GlobalConfig(value = "sleeping-block-entity", lock = true)
        public boolean sleepingBlockEntity = false;

        @GlobalConfig(value = "equipment-tracking", lock = true)
        public boolean equipmentTracking = false;
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
            @TransferConfig("protocol.pca-sync-protocol")
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

            @TransferConfig("protocol.pca-sync-player-entity")
            @GlobalConfig(value = "pca-sync-player-entity")
            public PcaPlayerEntityType syncPlayerEntity = PcaPlayerEntityType.OPS;

            public enum PcaPlayerEntityType {
                NOBODY, BOT, OPS, OPS_AND_SELF, EVERYONE
            }
        }

        public AppleSkinConfig appleskin = new AppleSkinConfig();

        @GlobalConfigCategory("appleskin")
        public static class AppleSkinConfig {
            @TransferConfig("protocol.appleskin-protocol")
            @GlobalConfig("protocol")
            public boolean enable = false;

            @GlobalConfig("sync-tick-interval")
            public int syncTickInterval = 20;
        }

        public ServuxConfig servux = new ServuxConfig();

        @GlobalConfigCategory("servux")
        public static class ServuxConfig {
            @TransferConfig("protocol.servux-protocol")
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

                @TransferConfig("protocol.servux.litematics-protocol")
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

            @SuppressWarnings("unused")
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

        @TransferConfig("no-chat-sign")
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

        @GlobalConfig(value = "force-minecraft-command", lock = true)
        public boolean forceMinecraftCommand = false;

        @GlobalConfig("leaves-packet-event")
        public boolean leavesPacketEvent = false;

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
        public boolean vanillaPortalHandle = true;

        @GlobalConfig("vanilla-fluid-pushing")
        public boolean vanillaFluidPushing = true;

        @GlobalConfig(value = "collision-behavior")
        public CollisionBehavior collisionBehavior = CollisionBehavior.BLOCK_SHAPE_VANILLA;

        public enum CollisionBehavior {
            VANILLA, BLOCK_SHAPE_VANILLA, PAPER
        }

        @GlobalConfig("vanilla-end-void-rings")
        public boolean vanillaEndVoidRings = false;

        @GlobalConfig("stacked-container-destroyed-drop")
        public boolean stackedContainerDestroyedDrop = true;
    }
}
