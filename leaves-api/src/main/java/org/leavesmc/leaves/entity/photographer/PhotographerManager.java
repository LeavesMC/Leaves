package org.leavesmc.leaves.entity.photographer;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.replay.BukkitRecorderOption;

import java.util.Collection;
import java.util.UUID;

public interface PhotographerManager {
    @Nullable Photographer getPhotographer(@NotNull UUID uuid);

    @Nullable Photographer getPhotographer(@NotNull String id);

    @Nullable Photographer createPhotographer(@NotNull String id, @NotNull Location location);

    @Nullable Photographer createPhotographer(@NotNull String id, @NotNull Location location, @NotNull BukkitRecorderOption recorderOption);

    void removePhotographer(@NotNull String id);

    void removePhotographer(@NotNull UUID uuid);

    void removeAllPhotographers();

    Collection<Photographer> getPhotographers();
}
