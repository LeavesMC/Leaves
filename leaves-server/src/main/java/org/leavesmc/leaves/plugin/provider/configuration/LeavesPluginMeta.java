// This file is licensed under the MIT license.
package org.leavesmc.leaves.plugin.provider.configuration;

import com.google.common.collect.ImmutableList;
import io.papermc.paper.configuration.constraint.Constraint;
import io.papermc.paper.configuration.serializer.ComponentSerializer;
import io.papermc.paper.configuration.serializer.EnumValueSerializer;
import io.papermc.paper.plugin.provider.configuration.FlattenedResolver;
import io.papermc.paper.plugin.provider.configuration.LegacyPaperMeta;
import io.papermc.paper.plugin.provider.configuration.PaperPluginMeta;
import io.papermc.paper.plugin.provider.configuration.serializer.PermissionConfigurationSerializer;
import io.papermc.paper.plugin.provider.configuration.serializer.constraints.PluginConfigConstraints;
import io.papermc.paper.plugin.provider.configuration.type.PermissionConfiguration;
import org.bukkit.craftbukkit.util.ApiVersion;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.util.function.Predicate;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public class LeavesPluginMeta extends PaperPluginMeta {
    private FeaturesConfiguration features = new FeaturesConfiguration();
    private MixinConfiguration mixin = new MixinConfiguration();
    static final ApiVersion MINIMUM = ApiVersion.getOrCreateVersion("1.21.4");

    public static LeavesPluginMeta create(BufferedReader reader) throws ConfigurateException {
        GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
            .source(() -> reader)
            .defaultOptions((options) ->
                options.serializers((serializers) ->
                    serializers.register(new ScalarSerializer<>(ApiVersion.class) {
                            @Override
                            public ApiVersion deserialize(final @NotNull Type type, final @NotNull Object obj) throws SerializationException {
                                try {
                                    final ApiVersion version = ApiVersion.getOrCreateVersion(obj.toString());
                                    if (version.isOlderThan(MINIMUM)) {
                                        throw new SerializationException(version + " is too old for a leaves plugin!");
                                    }
                                    return version;
                                } catch (final IllegalArgumentException e) {
                                    throw new SerializationException(e);
                                }
                            }

                            @Override
                            protected @NotNull Object serialize(final ApiVersion item, final @NotNull Predicate<Class<?>> typeSupported) {
                                return item.getVersionString();
                            }
                        })
                        .register(new EnumValueSerializer())
                        .register(PermissionConfiguration.class, PermissionConfigurationSerializer.SERIALIZER)
                        .register(new ComponentSerializer())
                        .registerAnnotatedObjects(
                            ObjectMapper.factoryBuilder()
                                .addConstraint(Constraint.class, new Constraint.Factory())
                                .addConstraint(PluginConfigConstraints.PluginName.class, String.class, new PluginConfigConstraints.PluginName.Factory())
                                .addConstraint(PluginConfigConstraints.PluginNameSpace.class, String.class, new PluginConfigConstraints.PluginNameSpace.Factory())
                                .addNodeResolver(new FlattenedResolver.Factory())
                                .build()
                        )
                )
            )
            .build();
        ConfigurationNode node = loader.load();
        LegacyPaperMeta.migrate(node);
        LeavesPluginMeta pluginConfiguration = node.require(LeavesPluginMeta.class);

        var authorNode = node.node("author");
        if (!authorNode.virtual()) {
            String author = authorNode.getString();
            var authorsBuilder = ImmutableList.<String>builder();
            if (author != null) {
                authorsBuilder.add(author);
            }
            pluginConfiguration.authors = authorsBuilder
                .addAll(pluginConfiguration.authors)
                .build();
        }

        return pluginConfiguration;
    }

    public FeaturesConfiguration getFeatures() {
        return features;
    }

    public MixinConfiguration getMixin() {
        return mixin;
    }
}
