package org.leavesmc.leaves.entity;

import org.bukkit.Location;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.botaction.CustomBotAction;
import org.leavesmc.leaves.replay.BukkitRecorderOption;

import java.util.Collection;
import java.util.UUID;

public interface PhotographerManager {
    @Nullable
    public Photographer getPhotographer(@NotNull UUID uuid);

    @Nullable
    public Photographer getPhotographer(@NotNull String id);

    @Nullable
    public Photographer createPhotographer(@NotNull String id, @NotNull Location location);

    @Nullable
    public Photographer createPhotographer(@NotNull String id, @NotNull Location location, @NotNull BukkitRecorderOption recorderOption);

    public void removePhotographer(@NotNull String id);

    public void removePhotographer(@NotNull UUID uuid);

    public void removeAllPhotographers();

    public Collection<Photographer> getPhotographers();
}
