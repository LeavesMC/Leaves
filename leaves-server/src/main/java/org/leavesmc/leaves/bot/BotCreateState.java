package org.leavesmc.leaves.bot;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.bot.Bot;
import org.leavesmc.leaves.entity.bot.BotCreator;
import org.leavesmc.leaves.entity.bot.CraftBot;
import org.leavesmc.leaves.event.bot.BotCreateEvent;
import org.leavesmc.leaves.plugin.MinecraftInternalPlugin;

import java.util.Objects;
import java.util.function.Consumer;

public record BotCreateState(String rawName, String fullName, String skinName, String[] skin, Location location, BotCreateEvent.CreateReason createReason, CommandSender creator) {

    private static final MinecraftServer server = MinecraftServer.getServer();

    public ServerBot createNow() {
        return server.getBotList().createNewBot(this);
    }

    @NotNull
    public static Builder builder(@NotNull String rawName, @Nullable Location location) {
        return new Builder(rawName, location);
    }

    public static class Builder implements BotCreator {

        private final String rawName; // For internal calculation, use it as little as possible

        private String fullName;
        private Location location;

        private String skinName;
        private String[] skin;

        private BotCreateEvent.CreateReason createReason;
        private CommandSender creator;

        private Builder(@NotNull String rawName, @Nullable Location location) {
            Objects.requireNonNull(rawName);

            this.rawName = rawName;
            this.location = location;

            this.fullName = BotUtil.getFullName(rawName);
            this.skinName = this.rawName;
            this.skin = null;
            this.createReason = BotCreateEvent.CreateReason.UNKNOWN;
            this.creator = null;
        }

        public Builder name(@NotNull String name) {
            Objects.requireNonNull(name);
            this.fullName = name;
            return this;
        }

        public Builder skinName(@Nullable String skinName) {
            this.skinName = skinName;
            return this;
        }

        public Builder skin(@Nullable String[] skin) {
            this.skin = skin;
            return this;
        }

        public Builder mojangAPISkin() {
            if (this.skinName != null) {
                this.skin = MojangAPI.getSkin(this.skinName);
            }
            return this;
        }

        public Builder location(@NotNull Location location) {
            this.location = location;
            return this;
        }

        public Builder createReason(@NotNull BotCreateEvent.CreateReason createReason) {
            Objects.requireNonNull(createReason);
            this.createReason = createReason;
            return this;
        }

        public Builder creator(CommandSender creator) {
            this.creator = creator;
            return this;
        }

        public BotCreateState build() {
            return new BotCreateState(rawName, fullName, skinName, skin, location, createReason, creator);
        }

        public void spawnWithSkin(Consumer<Bot> consumer) {
            Bukkit.getScheduler().runTaskAsynchronously(MinecraftInternalPlugin.INSTANCE, () -> {
                this.mojangAPISkin();
                Bukkit.getScheduler().runTask(MinecraftInternalPlugin.INSTANCE, () -> {
                    CraftBot bot = this.spawn();
                    if (bot != null && consumer != null) {
                        consumer.accept(bot);
                    }
                });
            });
        }

        @Nullable
        public CraftBot spawn() {
            Objects.requireNonNull(this.location);
            ServerBot bot = this.build().createNow();
            return bot != null ? bot.getBukkitEntity() : null;
        }
    }
}
