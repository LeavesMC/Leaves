package org.leavesmc.leaves.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.event.player.UpdateSuppressionEvent;

import java.util.ArrayList;
import java.util.List;

public class UpdateSuppressionException extends RuntimeException {
    private @Nullable BlockPos pos;
    private @Nullable Level level;
    private @Nullable Block source;
    private @Nullable ServerPlayer player;
    private final @NotNull Throwable throwable;

    public UpdateSuppressionException(
        @Nullable BlockPos pos,
        @Nullable Level level,
        @Nullable Block source,
        @Nullable ServerPlayer player,
        @NotNull Throwable throwable
    ) {
        super("Update Suppression");
        this.pos = pos;
        this.level = level;
        this.source = source;
        this.player = player;
        this.throwable = throwable;
    }

    public void providePlayer(@NotNull ServerPlayer player) {
        if (this.level == null) {
            this.level = player.level();
        }
        this.player = player;
    }

    public void provideLevel(@NotNull Level level) {
        if (this.level != null) {
            this.level = level;
        }
    }

    public void provideBlock(@NotNull Level level, @NotNull BlockPos pos, @NotNull Block source) {
        provideLevel(level);
        provideBlock(pos, source);
    }

    public void provideBlock(@NotNull BlockPos pos, @NotNull Block source) {
        if (this.pos != null) {
            this.pos = pos;
        }
        if (this.source != null) {
            this.source = source;
        }
    }

    public void consume() {
        submitEvent();
        LeavesLogger.LOGGER.info(getMessage());
    }

    private void submitEvent() {
        Location location = null;
        if (pos != null && level != null) {
            location = new Location(level.getWorld(), pos.getX(), pos.getY(), pos.getZ());
        }
        Material material = null;
        if (source != null) {
            material = source.defaultBlockState().getBukkitMaterial();
        }
        Player bukkitPlayer = null;
        if (player != null) {
            bukkitPlayer = player.getBukkitEntity();
        }
        new UpdateSuppressionEvent(bukkitPlayer, location, material, throwable).callEvent();
    }

    @Override
    public String getMessage() {
        List<String> messages = new ArrayList<>();
        messages.add("An %s update suppression was triggered".formatted(getTypeName()));
        if (source != null) {
            messages.add("from %s".formatted(source.defaultBlockState().getBukkitMaterial().name()));
        }
        if (pos != null) {
            messages.add("at [x:%d,y:%d,z:%d]".formatted(pos.getX(), pos.getY(), pos.getZ()));
        }
        if (level != null) {
            messages.add("in %s".formatted(level.dimension().identifier()));
        }
        if (player != null) {
            if (player instanceof ServerBot) {
                messages.add("by %s[bot]".formatted(player.displayName));
            } else {
                messages.add("by %s".formatted(player.displayName));
            }
        }
        return String.join(" ", messages);
    }

    @Contract(pure = true)
    private @NotNull String getTypeName() {
        Class<? extends Throwable> type = throwable.getClass();
        if (type == ClassCastException.class) {
            return "CCE";
        } else if (type == StackOverflowError.class) {
            return "SOE";
        } else if (type == IllegalArgumentException.class) {
            return "IAE";
        }
        return type.getSimpleName();
    }
}
