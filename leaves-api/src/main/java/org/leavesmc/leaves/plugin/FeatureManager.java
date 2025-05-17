// This file is licensed under the MIT license.
package org.leavesmc.leaves.plugin;

import java.util.Set;

public interface FeatureManager {
    Set<String> getAvailableFeatures();

    boolean isFeatureAvailable(String feature);
}
