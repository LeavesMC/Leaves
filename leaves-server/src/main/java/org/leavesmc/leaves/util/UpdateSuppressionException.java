package org.leavesmc.leaves.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.event.player.UpdateSuppressionEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class UpdateSuppressionException extends RuntimeException {
    private final @Nullable BlockPos pos;
    private final @Nullable Level level;
    private final @Nullable Block source;
    private final @Nullable ServerPlayer player;
    private final @NotNull Class<? extends Throwable> type;

    public UpdateSuppressionException(
        @Nullable BlockPos pos,
        @Nullable Level level,
        @Nullable Block source,
        @Nullable ServerPlayer player,
        @NotNull Class<? extends Throwable> type
    ) {
        super("Update Suppression");
        this.pos = pos;
        this.level = level;
        this.source = source;
        this.player = player;
        this.type = type;
    }

    public UpdateSuppressionException applyPlayer(@NotNull ServerPlayer player) {
        return new UpdateSuppressionException(this.pos, level != null ? level : player.level(), this.source, player, this.type);
    }

    public UpdateSuppressionException applyLevel(@NotNull Level level) {
        return new UpdateSuppressionException(this.pos, level, this.source, this.player, this.type);
    }

    public UpdateSuppressionException applyBlock(@NotNull Level level, @NotNull BlockPos pos, @NotNull Block source) {
        return new UpdateSuppressionException(pos, level, source, this.player, this.type);
    }

    public UpdateSuppressionException applyBlock(@NotNull BlockPos pos, @NotNull Block source) {
        return new UpdateSuppressionException(pos, this.level, source, this.player, this.type);
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
            material = CraftMagicNumbers.getMaterial(source);
        }
        Player bukkitPlayer = null;
        if (player != null) {
            bukkitPlayer = player.getBukkitEntity();
        }
        new UpdateSuppressionEvent(bukkitPlayer, location, material, type).callEvent();
    }

    public boolean noLevel() {
        return level == null;
    }

    public boolean noBlock() {
        return pos == null || source == null;
    }

    public boolean noPlayer() {
        return player == null;
    }

    @Override
    public String getMessage() {
        List<String> messages = new ArrayList<>();
        messages.add("An %s update suppression was triggered".formatted(getTypeName()));
        if (source != null) {
            messages.add("from %s".formatted(CraftMagicNumbers.getMaterial(source).name()));
        }
        if (pos != null) {
            messages.add("at [x:%d,y:%d,z:%d]".formatted(pos.getX(), pos.getY(), pos.getZ()));
        }
        if (level != null) {
            messages.add("in %s".formatted(level.dimension().location()));
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
