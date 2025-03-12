package org.leavesmc.leaves.entity;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface Photographer extends Player {

    @NotNull
    public String getId();

    public void setRecordFile(@NotNull File file);

    public void stopRecording();

    public void stopRecording(boolean async);

    public void stopRecording(boolean async, boolean save);

    public void pauseRecording();

    public void resumeRecording();

    public void setFollowPlayer(@Nullable Player player);
}
