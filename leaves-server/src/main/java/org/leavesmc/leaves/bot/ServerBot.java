package org.leavesmc.leaves.bot;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.EntityHitResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.bot.agent.AbstractBotAction;
import org.leavesmc.leaves.bot.agent.AbstractBotConfig;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.Configs;
import org.leavesmc.leaves.entity.CraftBot;
import org.leavesmc.leaves.event.bot.BotActionScheduleEvent;
import org.leavesmc.leaves.event.bot.BotCreateEvent;
import org.leavesmc.leaves.event.bot.BotDeathEvent;
import org.leavesmc.leaves.event.bot.BotInventoryOpenEvent;
import org.leavesmc.leaves.event.bot.BotRemoveEvent;
import org.leavesmc.leaves.plugin.MinecraftInternalPlugin;
import org.leavesmc.leaves.util.MathUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public class ServerBot extends ServerPlayer {

    private final List<AbstractBotAction<?>> actions;
    private final Map<Configs<?>, AbstractBotConfig<?>> configs;

    public boolean resume = false;
    public BotCreateState createState;
    public UUID createPlayer;

    private final int tracingRange;
    private final BotStatsCounter stats;
    private final BotInventoryContainer container;

    public int notSleepTicks;

    public int removeTaskId = -1;

    public ServerBot(MinecraftServer server, ServerLevel world, GameProfile profile) {
        super(server, world, profile, ClientInformation.createDefault());
        this.entityData.set(Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte) -2);

        this.gameMode = new ServerBotGameMode(this);

        this.actions = new ArrayList<>();
        ImmutableMap.Builder<Configs<?>, AbstractBotConfig<?>> configBuilder = ImmutableMap.builder();
        for (Configs<?> config : Configs.getConfigs()) {
            configBuilder.put(config, config.createConfig(this));
        }
        this.configs = configBuilder.build();

        this.stats = new BotStatsCounter(server);
        this.container = new BotInventoryContainer(this);
        this.tracingRange = world.spigotConfig.playerTrackingRange * world.spigotConfig.playerTrackingRange;

        this.notSleepTicks = 0;
        this.fauxSleeping = LeavesConfig.modify.fakeplayer.canSkipSleep;
        this.setClientLoaded(true);
    }

    @Override
    public void tick() {
        if (!this.isAlive()) {
            return;
        }

        if (this.getConfigValue(Configs.TICK_TYPE) == TickType.ENTITY_LIST) {
            this.runAction();
        }

        // copy ServerPlayer start
        if (this.joining) {
            this.joining = false;
        }

        this.gameMode.tick();
        this.wardenSpawnTracker.tick();
        if (this.invulnerableTime > 0) {
            this.invulnerableTime--;
        }
        // copy ServerPlayer end

        if (this.getConfigValue(Configs.SPAWN_PHANTOM)) {
            notSleepTicks++;
        }

        if (LeavesConfig.modify.fakeplayer.regenAmount > 0.0 && server.getTickCount() % 20 == 0) {
            float health = getHealth();
            float maxHealth = getMaxHealth();
            float regenAmount = (float) (LeavesConfig.modify.fakeplayer.regenAmount * 20);
            float amount;

            if (health < maxHealth - regenAmount) {
                amount = health + regenAmount;
            } else {
                amount = maxHealth;
            }

            this.setHealth(amount);
        }

        if (this.getConfigValue(Configs.TICK_TYPE) == TickType.ENTITY_LIST) {
            this.doTick();
        }
    }

    @Override
    public void doTick() {
        this.absMoveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());

        if (this.isPassenger()) {
            this.setOnGround(false);
        }

        if (this.takeXpDelay > 0) {
            --this.takeXpDelay;
        }

        if (this.isSleeping()) {
            ++this.sleepCounter;
            if (this.sleepCounter > 100) {
                this.sleepCounter = 100;
                this.notSleepTicks = 0;
            }

            if (!this.level().isClientSide && this.level().isDay()) {
                this.stopSleepInBed(false, true);
            }
        } else if (this.sleepCounter > 0) {
            ++this.sleepCounter;
            if (this.sleepCounter >= 110) {
                this.sleepCounter = 0;
            }
        }

        this.updateIsUnderwater();

        if (this.getConfigValue(Configs.TICK_TYPE) == TickType.NETWORK) {
            this.server.scheduleOnMain(this::runAction);
        }

        this.livingEntityTick();

        this.foodData.tick(this);

        double d = Mth.clamp(this.getX(), -2.9999999E7, 2.9999999E7);
        double d1 = Mth.clamp(this.getZ(), -2.9999999E7, 2.9999999E7);
        if (d != this.getX() || d1 != this.getZ()) {
            this.setPos(d, this.getY(), d1);
        }

        ++this.attackStrengthTicker;
        ItemStack itemstack = this.getMainHandItem();
        if (!ItemStack.matches(this.lastItemInMainHand, itemstack)) {
            if (!ItemStack.isSameItem(this.lastItemInMainHand, itemstack)) {
                this.resetAttackStrengthTicker();
            }

            this.lastItemInMainHand = itemstack.copy();
        }

        this.getCooldowns().tick();
        this.updatePlayerPose();
    }

    @Override
    public @Nullable ServerBot teleport(@NotNull TeleportTransition teleportTarget) {
        if (this.isSleeping() || this.isRemoved()) {
            return null;
        }
        if (teleportTarget.newLevel().dimension() != this.serverLevel().dimension()) {
            return null;
        } else {
            if (!teleportTarget.asPassenger()) {
                this.stopRiding();
            }

            this.connection.internalTeleport(PositionMoveRotation.of(teleportTarget), teleportTarget.relatives());
            this.connection.resetPosition();
            teleportTarget.postTeleportTransition().onTransition(this);
            return this;
        }
    }

    @Override
    public void handlePortal() {
    }

    @Override
    public void onItemPickup(@NotNull ItemEntity item) {
        super.onItemPickup(item);
        this.updateItemInHand(InteractionHand.MAIN_HAND);
    }

    public void updateItemInHand(InteractionHand hand) {
        ItemStack item = this.getItemInHand(hand);

        if (!item.isEmpty()) {
            BotUtil.replenishment(item, getInventory().items);
            if (BotUtil.isDamage(item, 10)) {
                BotUtil.replaceTool(hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, this);
            }
        }
        this.detectEquipmentUpdatesPublic();
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (LeavesConfig.modify.fakeplayer.canOpenInventory) {
            if (player instanceof ServerPlayer player1 && player.getMainHandItem().isEmpty()) {
                BotInventoryOpenEvent event = new BotInventoryOpenEvent(this.getBukkitEntity(), player1.getBukkitEntity());
                this.server.server.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    player.openMenu(new SimpleMenuProvider((i, inventory, p) -> ChestMenu.sixRows(i, inventory, this.container), this.getDisplayName()));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.interact(player, hand);
    }

    @Override
    public void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
        ServerLevel serverLevel = this.serverLevel();
        if (onGround && this.fallDistance > 0.0F) {
            this.onChangedBlock(serverLevel, pos);
            double attributeValue = this.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
            if (this.fallDistance > attributeValue && !state.isAir()) {
                double x = this.getX();
                double y1 = this.getY();
                double z = this.getZ();
                BlockPos blockPos = this.blockPosition();
                if (pos.getX() != blockPos.getX() || pos.getZ() != blockPos.getZ()) {
                    double d = x - pos.getX() - 0.5;
                    double d1 = z - pos.getZ() - 0.5;
                    double max = Math.max(Math.abs(d), Math.abs(d1));
                    x = pos.getX() + 0.5 + d / max * 0.5;
                    z = pos.getZ() + 0.5 + d1 / max * 0.5;
                }

                float f = Mth.ceil(this.fallDistance - attributeValue);
                double min = Math.min(0.2F + f / 15.0F, 2.5);
                int i = (int) (150.0 * min);
                serverLevel.sendParticlesSource(this, new BlockParticleOption(ParticleTypes.BLOCK, state), false, false, x, y1, z, i, 0.0, 0.0, 0.0, 0.15F);
            }
        }

        if (onGround) {
            if (this.fallDistance > 0.0F) {
                state.getBlock().fallOn(serverLevel, state, pos, this, this.fallDistance);
                serverLevel.gameEvent(GameEvent.HIT_GROUND, this.position(),
                    GameEvent.Context.of(this, this.mainSupportingBlockPos.map(supportingPos -> this.level().getBlockState(supportingPos)).orElse(state))
                );
            }

            this.resetFallDistance();
        } else if (y < 0.0D) {
            this.fallDistance -= (float) y;
        }
    }

    @Override
    public void attack(@NotNull Entity target) {
        super.attack(target);
        this.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("isShiftKeyDown", this.isShiftKeyDown());

        CompoundTag createNbt = new CompoundTag();
        createNbt.putString("realName", this.createState.realName());
        createNbt.putString("name", this.createState.name());

        createNbt.putString("skinName", this.createState.skinName());
        if (this.createState.skin() != null) {
            ListTag skin = new ListTag();
            for (String s : this.createState.skin()) {
                skin.add(StringTag.valueOf(s));
            }
            createNbt.put("skin", skin);
        }

        nbt.put("createStatus", createNbt);

        if (!this.actions.isEmpty()) {
            ListTag actionNbt = new ListTag();
            for (AbstractBotAction<?> action : this.actions) {
                actionNbt.add(action.save(new CompoundTag()));
            }
            nbt.put("actions", actionNbt);
        }

        if (!this.configs.isEmpty()) {
            ListTag configNbt = new ListTag();
            for (AbstractBotConfig<?> config : this.configs.values()) {
                configNbt.add(config.save(new CompoundTag()));
            }
            nbt.put("configs", configNbt);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.setShiftKeyDown(nbt.getBoolean("isShiftKeyDown"));

        CompoundTag createNbt = nbt.getCompound("createStatus");
        BotCreateState.Builder createBuilder = BotCreateState.builder(createNbt.getString("realName"), null).name(createNbt.getString("name"));

        String[] skin = null;
        if (createNbt.contains("skin")) {
            ListTag skinTag = createNbt.getList("skin", 8);
            skin = new String[skinTag.size()];
            for (int i = 0; i < skinTag.size(); i++) {
                skin[i] = skinTag.getString(i);
            }
        }

        createBuilder.skinName(createNbt.getString("skinName")).skin(skin);
        createBuilder.createReason(BotCreateEvent.CreateReason.INTERNAL).creator(null);

        this.createState = createBuilder.build();
        this.gameProfile = new BotList.CustomGameProfile(this.getUUID(), this.createState.name(), this.createState.skin());


        if (nbt.contains("actions")) {
            ListTag actionNbt = nbt.getList("actions", 10);
            for (int i = 0; i < actionNbt.size(); i++) {
                CompoundTag actionTag = actionNbt.getCompound(i);
                AbstractBotAction<?> action = Actions.getForName(actionTag.getString("actionName"));
                if (action != null) {
                    AbstractBotAction<?> newAction = action.create();
                    newAction.load(actionTag);
                    this.actions.add(newAction);
                }
            }
        }

        if (nbt.contains("configs")) {
            ListTag configNbt = nbt.getList("configs", 10);
            for (int i = 0; i < configNbt.size(); i++) {
                CompoundTag configTag = configNbt.getCompound(i);
                Configs<?> configKey = Configs.getConfig(configTag.getString("configName"));
                if (configKey != null) {
                    this.configs.get(configKey).load(configTag);
                }
            }
        }
    }

    public void sendPlayerInfo(ServerPlayer player) {
        player.connection.send(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME), List.of(this)));
    }

    public boolean needSendFakeData(ServerPlayer player) {
        return this.getConfigValue(Configs.ALWAYS_SEND_DATA) && (player.level() == this.level() && player.position().distanceToSqr(this.position()) > this.tracingRange);
    }

    public void sendFakeDataIfNeed(ServerPlayer player, boolean login) {
        if (needSendFakeData(player)) {
            this.sendFakeData(player.connection, login);
        }
    }

    public void sendFakeData(ServerPlayerConnection playerConnection, boolean login) {
        ChunkMap.TrackedEntity entityTracker = ((ServerLevel) this.level()).getChunkSource().chunkMap.entityMap.get(this.getId());

        if (entityTracker == null) {
            LeavesLogger.LOGGER.warning("Fakeplayer cant get entity tracker for " + this.getId());
            return;
        }

        playerConnection.send(this.getAddEntityPacket(entityTracker.serverEntity));
        if (login) {
            Bukkit.getScheduler().runTaskLater(MinecraftInternalPlugin.INSTANCE, () -> playerConnection.send(new ClientboundRotateHeadPacket(this, (byte) ((getYRot() * 256f) / 360f))), 10);
        } else {
            playerConnection.send(new ClientboundRotateHeadPacket(this, (byte) ((getYRot() * 256f) / 360f)));
        }
    }

    public void renderAll() {
        this.server.getPlayerList().getPlayers().forEach(
            player -> {
                this.sendPlayerInfo(player);
                this.sendFakeDataIfNeed(player, false);
            }
        );
    }

    private void sendPacket(Packet<?> packet) {
        this.server.getPlayerList().getPlayers().forEach(player -> player.connection.send(packet));
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        boolean flag = this.serverLevel().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
        Component defaultMessage = this.getCombatTracker().getDeathMessage();

        BotDeathEvent event = new BotDeathEvent(this.getBukkitEntity(), PaperAdventure.asAdventure(defaultMessage), flag);
        this.server.server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            if (this.getHealth() <= 0) {
                this.setHealth(0.1f);
            }
            return;
        }

        this.gameEvent(GameEvent.ENTITY_DIE);

        net.kyori.adventure.text.Component deathMessage = event.deathMessage();
        if (event.isSendDeathMessage() && deathMessage != null && !deathMessage.equals(net.kyori.adventure.text.Component.empty())) {
            this.server.getPlayerList().broadcastSystemMessage(PaperAdventure.asVanilla(deathMessage), false);
        }

        this.server.getBotList().removeBot(this, BotRemoveEvent.RemoveReason.DEATH, null, false);
    }

    public void removeTab() {
        this.sendPacket(new ClientboundPlayerInfoRemovePacket(List.of(this.getUUID())));
    }

    public void faceLocation(@NotNull Location loc) {
        this.look(loc.toVector().subtract(getLocation().toVector()), false);
    }

    public void look(Vector dir, boolean keepYaw) {
        float yaw, pitch;

        if (keepYaw) {
            yaw = this.getYHeadRot();
            pitch = MathUtils.fetchPitch(dir);
        } else {
            float[] vals = MathUtils.fetchYawPitch(dir);
            yaw = vals[0];
            pitch = vals[1];

            this.sendPacket(new ClientboundRotateHeadPacket(this, (byte) (yaw * 256 / 360f)));
        }

        this.setRot(yaw, pitch);
    }

    public Location getLocation() {
        return this.getBukkitEntity().getLocation();
    }

    public Entity getTargetEntity(int maxDistance, Predicate<? super Entity> predicate) {
        List<Entity> entities = this.level().getEntities((Entity) null, this.getBoundingBox(), (e -> e != this && (predicate == null || predicate.test(e))));
        if (!entities.isEmpty()) {
            return entities.getFirst();
        } else {
            EntityHitResult result = this.getBukkitEntity().rayTraceEntity(maxDistance, false);
            if (result != null && (predicate == null || predicate.test(result.getEntity()))) {
                return result.getEntity();
            }
        }
        return null;
    }

    public void dropAll() {
        this.getInventory().dropAll();
        this.detectEquipmentUpdatesPublic();
    }

    private void runAction() {
        if (LeavesConfig.modify.fakeplayer.canUseAction) {
            this.actions.forEach(action -> action.tryTick(this));
            this.actions.removeIf(AbstractBotAction::isCancelled);
        }
    }

    public boolean addBotAction(AbstractBotAction<?> action, CommandSender sender) {
        if (!LeavesConfig.modify.fakeplayer.canUseAction) {
            return false;
        }

        if (!new BotActionScheduleEvent(this.getBukkitEntity(), action.getName(), action.getUUID(), sender).callEvent()) {
            return false;
        }

        action.init();
        this.actions.add(action);
        return true;
    }

    public List<AbstractBotAction<?>> getBotActions() {
        return actions;
    }

    @Override
    @NotNull
    public ServerStatsCounter getStats() {
        return stats;
    }

    @SuppressWarnings("unchecked")
    public <E> AbstractBotConfig<E> getConfig(Configs<E> config) {
        return (AbstractBotConfig<E>) Objects.requireNonNull(this.configs.get(config));
    }

    public <E> E getConfigValue(Configs<E> config) {
        return this.getConfig(config).getValue();
    }

    @Override
    @NotNull
    public CraftBot getBukkitEntity() {
        return (CraftBot) super.getBukkitEntity();
    }

    public enum TickType {
        NETWORK,
        ENTITY_LIST
    }
}
