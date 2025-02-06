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
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

@ConfigSerializable
public class LeavesPluginMeta extends PaperPluginMeta {
    private List<String> mixins;
    static final ApiVersion MINIMUM = ApiVersion.getOrCreateVersion("1.21");

    public static LeavesPluginMeta create(BufferedReader reader) throws ConfigurateException {
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .prettyPrinting(true)
                .emitComments(true)
                .emitJsonCompatible(true)
                .source(() -> reader)
                .defaultOptions((options) ->
                        options.serializers((serializers) ->
                                serializers.register(new ScalarSerializer<>(ApiVersion.class) {
                                            @Override
                                            public ApiVersion deserialize(final Type type, final Object obj) throws SerializationException {
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
                                            protected Object serialize(final ApiVersion item, final Predicate<Class<?>> typeSupported) {
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
        CommentedConfigurationNode node = loader.load();
        LegacyPaperMeta.migrate(node);
        LeavesPluginMeta pluginConfiguration = node.require(LeavesPluginMeta.class);

        if (!node.node("author").virtual()) {
            pluginConfiguration.authors = ImmutableList.<String>builder()
                    .addAll(pluginConfiguration.authors)
                    .add(node.node("author").getString())
                    .build();
        }

        return pluginConfiguration;
    }

    public List<String> getMixins() {
        return mixins;
    }
}
