package org.leavesmc.leaves.lithium.common.tracking.entity;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ChunkSectionEntityMovementTracker {
    protected long lastChangeTime = 0;
    protected final ReferenceOpenHashSet<ChunkSectionEntityMovementListener> listeners = new ReferenceOpenHashSet<>();
    protected final long sectionKey;
    protected int userCount = 0;

    public ChunkSectionEntityMovementTracker(long sectionKey) {
        this.sectionKey = sectionKey;
    }

    public void register() {
        this.userCount++;
    }

    public abstract void unregister();

    public static void unregister(@NotNull List<? extends ChunkSectionEntityMovementTracker> trackers) {
        for (ChunkSectionEntityMovementTracker tracker : trackers) {
            tracker.unregister();
        }
    }

    public boolean isUnchangedSince(long lastCheckedTime) {
        return this.lastChangeTime <= lastCheckedTime;
    }

    public static boolean isUnchangedSince(long lastCheckedTime, @NotNull List<? extends ChunkSectionEntityMovementTracker> trackers) {
        for (ChunkSectionEntityMovementTracker tracker : trackers) {
            if (!tracker.isUnchangedSince(lastCheckedTime)) {
                return false;
            }
        }
        return true;
    }

    public void listenToEntityMovementOnce(ChunkSectionEntityMovementListener listener) {
        this.listeners.add(listener);
    }

    public static void listenToEntityMovementOnce(ChunkSectionEntityMovementListener listener, @NotNull List<? extends ChunkSectionEntityMovementTracker> trackers) {
        for (ChunkSectionEntityMovementTracker tracker : trackers) {
            tracker.listenToEntityMovementOnce(listener);
        }
    }

    private void setChanged(long atTime) {
        if (atTime > this.lastChangeTime) {
            this.lastChangeTime = atTime;
        }
    }

    public void notifyAllListeners(long time) {
        if (!listeners.isEmpty()) {
            for (ChunkSectionEntityMovementListener listener : listeners) {
                listener.handleEntityMovement();
            }
            listeners.clear();
        }
        setChanged(time);
    }
}
