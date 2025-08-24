package org.leavesmc.leaves.bot;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.logging.LogUtils;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.event.bot.BotCreateEvent;
import org.leavesmc.leaves.event.bot.BotJoinEvent;
import org.leavesmc.leaves.event.bot.BotLoadEvent;
import org.leavesmc.leaves.event.bot.BotRemoveEvent;
import org.leavesmc.leaves.event.bot.BotSpawnLocationEvent;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BotList {

    public static BotList INSTANCE;

    private static final Logger LOGGER = LogUtils.getLogger();

    private final MinecraftServer server;

    public final List<ServerBot> bots = new CopyOnWriteArrayList<>();
    private final BotDataStorage dataStorage;

    private final Map<UUID, ServerBot> botsByUUID = Maps.newHashMap();
    private final Map<String, ServerBot> botsByName = Maps.newHashMap();
    private final Map<String, Set<String>> botsNameByWorldUuid = Maps.newHashMap();

    public BotList(MinecraftServer server) {
        this.server = server;
        this.dataStorage = new BotDataStorage(server.storageSource);
        INSTANCE = this;
    }

    public ServerBot createNewBot(BotCreateState state) {
        BotCreateEvent event = new BotCreateEvent(state.name(), state.skinName(), state.location(), state.createReason(), state.creator());
        event.setCancelled(!BotUtil.isCreateLegal(state.name()));
        this.server.server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return null;
        }

        Location location = event.getCreateLocation();
        ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();

        CustomGameProfile profile = new CustomGameProfile(BotUtil.getBotUUID(state), state.name(), state.skin());
        ServerBot bot = new ServerBot(this.server, world, profile);
        bot.createState = state;
        if (event.getCreator() instanceof org.bukkit.entity.Player player) {
            bot.createPlayer = player.getUniqueId();
        }

        return this.placeNewBot(bot, world, location, null);
    }

    public ServerBot loadNewBot(String realName) {
        try {
            return this.loadNewBot(realName, this.dataStorage);
        } catch (Exception e) {
            LOGGER.error("Failed to load bot {}", realName, e);
            return null;
        }
    }

    public ServerBot loadNewBot(String realName, IPlayerDataStorage playerIO) {
        UUID uuid = BotUtil.getBotUUID(realName);

        BotLoadEvent event = new BotLoadEvent(realName, uuid);
        this.server.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return null;
        }

        ServerBot bot = new ServerBot(this.server, this.server.getLevel(Level.OVERWORLD), new GameProfile(uuid, realName));
        bot.connection = new ServerBotPacketListenerImpl(this.server, bot);
        Optional<ValueInput> optional;
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(bot.problemPath(), LOGGER)) {
            optional = playerIO.load(bot, scopedCollector);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (optional.isEmpty()) {
            return null;
        }
        ValueInput nbt = optional.get();

        ResourceKey<Level> resourcekey = null;
        if (nbt.getLong("WorldUUIDMost").isPresent() && nbt.getLong("WorldUUIDLeast").isPresent()) {
            org.bukkit.World bWorld = Bukkit.getServer().getWorld(new UUID(nbt.getLong("WorldUUIDMost").orElseThrow(), nbt.getLong("WorldUUIDLeast").orElseThrow()));
            if (bWorld != null) {
                resourcekey = ((CraftWorld) bWorld).getHandle().dimension();
            }
        }
        if (resourcekey == null) {
            return null;
        }

        ServerLevel world = this.server.getLevel(resourcekey);
        return this.placeNewBot(bot, world, bot.getLocation(), nbt);
    }

    public ServerBot placeNewBot(@NotNull ServerBot bot, ServerLevel world, Location location, ValueInput save) {
        Optional<ValueInput> optional = Optional.ofNullable(save);

        bot.isRealPlayer = true;
        bot.loginTime = System.currentTimeMillis();
        bot.connection = new ServerBotPacketListenerImpl(this.server, bot);
        bot.setServerLevel(world);

        BotSpawnLocationEvent event = new BotSpawnLocationEvent(bot.getBukkitEntity(), location);
        this.server.server.getPluginManager().callEvent(event);
        location = event.getSpawnLocation();

        bot.spawnIn(world);
        bot.gameMode.setLevel(bot.level());

        bot.setPosRaw(location.getX(), location.getY(), location.getZ());
        bot.setRot(location.getYaw(), location.getPitch());

        bot.connection.teleport(bot.getX(), bot.getY(), bot.getZ(), bot.getYRot(), bot.getXRot());

        this.bots.add(bot);
        this.botsByName.put(bot.getScoreboardName().toLowerCase(Locale.ROOT), bot);
        this.botsByUUID.put(bot.getUUID(), bot);

        bot.supressTrackerForLogin = true;
        world.addNewPlayer(bot);
        optional.ifPresent(nbt -> {
            bot.loadAndSpawnEnderPearls(nbt);
            bot.loadAndSpawnParentVehicle(nbt);
        });

        BotJoinEvent event1 = new BotJoinEvent(bot.getBukkitEntity(), PaperAdventure.asAdventure(Component.translatable("multiplayer.player.joined", bot.getDisplayName())).style(Style.style(NamedTextColor.YELLOW)));
        this.server.server.getPluginManager().callEvent(event1);

        net.kyori.adventure.text.Component joinMessage = event1.joinMessage();
        if (joinMessage != null && !joinMessage.equals(net.kyori.adventure.text.Component.empty())) {
            this.server.getPlayerList().broadcastSystemMessage(PaperAdventure.asVanilla(joinMessage), false);
        }

        bot.renderInfo();
        bot.supressTrackerForLogin = false;

        bot.level().getChunkSource().chunkMap.addEntity(bot);
        bot.renderData();
        bot.initInventoryMenu();
        botsNameByWorldUuid
            .computeIfAbsent(bot.level().uuid.toString(), (k) -> new HashSet<>())
            .add(bot.getBukkitEntity().getRealName());
        BotList.LOGGER.info("{}[{}] logged in with entity id {} at ([{}]{}, {}, {})", bot.getName().getString(), "Local", bot.getId(), bot.level().serverLevelData.getLevelName(), bot.getX(), bot.getY(), bot.getZ());
        return bot;
    }

    public boolean removeBot(@NotNull ServerBot bot, @NotNull BotRemoveEvent.RemoveReason reason, @Nullable CommandSender remover, boolean saved) {
        return this.removeBot(bot, reason, remover, saved, this.dataStorage);
    }

    public boolean removeBot(@NotNull ServerBot bot, @NotNull BotRemoveEvent.RemoveReason reason, @Nullable CommandSender remover, boolean saved, IPlayerDataStorage playerIO) {
        BotRemoveEvent event = new BotRemoveEvent(bot.getBukkitEntity(), reason, remover, PaperAdventure.asAdventure(Component.translatable("multiplayer.player.left", bot.getDisplayName())).style(Style.style(NamedTextColor.YELLOW)), saved);
        this.server.server.getPluginManager().callEvent(event);

        if (event.isCancelled() && event.getReason() != BotRemoveEvent.RemoveReason.INTERNAL) {
            return false;
        }

        if (bot.removeTaskId != -1) {
            Bukkit.getScheduler().cancelTask(bot.removeTaskId);
            bot.removeTaskId = -1;
        }

        bot.disconnect();

        if (event.shouldSave()) {
            playerIO.save(bot);
        } else {
            bot.dropAll(true);
            botsNameByWorldUuid.getOrDefault(bot.level().uuid.toString(), new HashSet<>()).remove(bot.getBukkitEntity().getRealName());
        }

        if (bot.isPassenger() && event.shouldSave()) {
            Entity entity = bot.getRootVehicle();
            if (entity.hasExactlyOnePlayerPassenger()) {
                bot.stopRiding();
                entity.getPassengersAndSelf().forEach((entity1) -> {
                    if (!org.leavesmc.leaves.LeavesConfig.modify.oldMC.voidTrade && entity1 instanceof AbstractVillager villager) {
                        final Player human = villager.getTradingPlayer();
                        if (human != null) {
                            villager.setTradingPlayer(null);
                        }
                    }
                    entity1.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                });
            }
        }

        bot.unRide();
        for (ThrownEnderpearl thrownEnderpearl : bot.getEnderPearls()) {
            if (!thrownEnderpearl.level().paperConfig().misc.legacyEnderPearlBehavior) {
                thrownEnderpearl.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER, EntityRemoveEvent.Cause.PLAYER_QUIT);
            } else {
                thrownEnderpearl.setOwner(null);
            }
        }

        bot.level().removePlayerImmediately(bot, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        this.bots.remove(bot);
        this.botsByName.remove(bot.getScoreboardName().toLowerCase(Locale.ROOT));

        UUID uuid = bot.getUUID();
        ServerBot bot1 = this.botsByUUID.get(uuid);
        if (bot1 == bot) {
            this.botsByUUID.remove(uuid);
        }

        bot.removeTab();
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(bot.getId());
        for (ServerPlayer player : bot.level().players()) {
            if (!(player instanceof ServerBot)) {
                player.connection.send(packet);
            }
        }

        net.kyori.adventure.text.Component removeMessage = event.removeMessage();
        if (removeMessage != null && !removeMessage.equals(net.kyori.adventure.text.Component.empty())) {
            this.server.getPlayerList().broadcastSystemMessage(PaperAdventure.asVanilla(removeMessage), false);
        }
        return true;
    }

    public void removeAllIn(String worldUuid) {
        for (String realName : this.botsNameByWorldUuid.getOrDefault(worldUuid, new HashSet<>())) {
            ServerBot bot = this.getBotByName(realName);
            if (bot != null) {
                this.removeBot(bot, BotRemoveEvent.RemoveReason.INTERNAL, null, LeavesConfig.modify.fakeplayer.canResident);
            }
        }
    }

    public void removeAll() {
        for (ServerBot bot : this.bots) {
            bot.resume = LeavesConfig.modify.fakeplayer.canResident;
            this.removeBot(bot, BotRemoveEvent.RemoveReason.INTERNAL, null, LeavesConfig.modify.fakeplayer.canResident);
        }
    }

    public void loadBotInfo() {
        if (!LeavesConfig.modify.fakeplayer.enable || !LeavesConfig.modify.fakeplayer.canResident) {
            return;
        }
        CompoundTag savedBotList = this.getSavedBotList().copy();
        for (String realName : savedBotList.keySet()) {
            CompoundTag nbt = savedBotList.getCompound(realName).orElseThrow();
            if (!nbt.getBoolean("resume").orElse(false)) {
                continue;
            }
            UUID levelUuid = BotUtil.getBotLevel(realName, this.dataStorage);
            if (levelUuid == null) {
                LOGGER.warn("Bot {} has no world UUID, skipping loading.", realName);
                continue;
            }
            this.botsNameByWorldUuid
                .computeIfAbsent(levelUuid.toString(), (k) -> new HashSet<>())
                .add(realName);
        }
    }

    public void loadResume(String worldUuid) {
        if (!LeavesConfig.modify.fakeplayer.enable || !LeavesConfig.modify.fakeplayer.canResident) {
            return;
        }
        Set<String> bots = this.botsNameByWorldUuid.get(worldUuid);
        if (bots == null) {
            return;
        }
        Set<String> botsCopy = new HashSet<>(bots);
        botsCopy.forEach(this::loadNewBot);
    }

    public void updateBotLevel(ServerBot bot, ServerLevel level) {
        String prevUuid = bot.level().uuid.toString();
        String newUuid = level.uuid.toString();
        this.botsNameByWorldUuid
            .computeIfAbsent(newUuid, (k) -> new HashSet<>())
            .add(bot.getBukkitEntity().getRealName());
        this.botsNameByWorldUuid
            .computeIfAbsent(prevUuid, (k) -> new HashSet<>())
            .remove(bot.getBukkitEntity().getRealName());
    }

    public void networkTick() {
        this.bots.forEach(ServerBot::networkTick);
    }

    @Nullable
    public ServerBot getBot(@NotNull UUID uuid) {
        return this.botsByUUID.get(uuid);
    }

    @Nullable
    public ServerBot getBotByName(@NotNull String name) {
        return this.botsByName.get(name.toLowerCase(Locale.ROOT));
    }

    public CompoundTag getSavedBotList() {
        return this.dataStorage.getSavedBotList();
    }

    public static class CustomGameProfile extends GameProfile {

        public CustomGameProfile(UUID uuid, String name, String[] skin) {
            super(uuid, name);
            this.setSkin(skin);
        }

        public void setSkin(String[] skin) {
            if (skin != null) {
                this.getProperties().put("textures", new Property("textures", skin[0], skin[1]));
            }
        }
    }
}
