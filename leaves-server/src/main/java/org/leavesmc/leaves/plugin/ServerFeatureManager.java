// This file is licensed under the MIT license.
package org.leavesmc.leaves.plugin;

import java.util.HashSet;
import java.util.Set;

import static org.leavesmc.leaves.plugin.Features.*;

public class ServerFeatureManager implements FeatureManager {
    public static ServerFeatureManager INSTANCE = new ServerFeatureManager();
    private final Set<String> availableFeatures = new HashSet<>();

    private ServerFeatureManager() {
        availableFeatures.addAll(Set.of(
            FAKEPLAYER,
            PHOTOGRAPHER,
            BYTEBUF,
            UPDATE_SUPPRESSION_EVENT
        ));
        if (Boolean.getBoolean("leavesclip.enable.mixin")) {
            availableFeatures.add(MIXIN);
        }
    }

    @Override
    public Set<String> getAvailableFeatures() {
        return availableFeatures;
    }

    @Override
    public boolean isFeatureAvailable(String feature) {
        return availableFeatures.contains(feature);
    }
}
