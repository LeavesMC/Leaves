// This file is licensed under the MIT license.
package org.leavesmc.leaves.plugin.provider.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;

import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
@ConfigSerializable
public class MixinConfiguration {
    private String packageName;
    private List<String> mixins = List.of();
    private String accessWidener;

    @PostProcess
    public void postProcess() {
        if (mixins.isEmpty()) {
            return;
        }
        if (packageName == null) {
            throw new IllegalStateException("Already define mixins: " + mixins + ", but no mixin package-name provided");
        }
    }

    public List<String> getMixins() {
        return mixins;
    }

    public String getAccessWidener() {
        return accessWidener;
    }

    public String getPackageName() {
        return packageName;
    }
}
