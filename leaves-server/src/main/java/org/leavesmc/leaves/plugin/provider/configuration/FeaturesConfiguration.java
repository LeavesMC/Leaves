// This file is licensed under the MIT license.
package org.leavesmc.leaves.plugin.provider.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public class FeaturesConfiguration {
    private List<String> required = List.of();
    private List<String> optional = List.of();

    public List<String> getRequired() {
        return required;
    }

    public List<String> getOptional() {
        return optional;
    }
}