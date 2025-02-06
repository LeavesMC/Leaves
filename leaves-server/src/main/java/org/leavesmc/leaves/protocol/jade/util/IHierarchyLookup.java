package org.leavesmc.leaves.protocol.jade.util;

import com.google.common.collect.Streams;
import net.minecraft.core.IdMapper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.protocol.jade.provider.IJadeProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface IHierarchyLookup<T extends IJadeProvider> {
    default IHierarchyLookup<? extends T> cast() {
        return this;
    }

    void idMapped();

    @Nullable
    IdMapper<T> idMapper();

    default List<ResourceLocation> mappedIds() {
        return Streams.stream(Objects.requireNonNull(idMapper()))
                .map(IJadeProvider::getUid)
                .toList();
    }

    void register(Class<?> clazz, T provider);

    boolean isClassAcceptable(Class<?> clazz);

    default List<T> get(Object obj) {
        if (obj == null) {
            return List.of();
        }
        return get(obj.getClass());
    }

    List<T> get(Class<?> clazz);

    boolean isEmpty();

    Stream<Map.Entry<Class<?>, Collection<T>>> entries();

    void invalidate();

    void loadComplete(PriorityStore<ResourceLocation, IJadeProvider> priorityStore);

    default IdMapper<T> createIdMapper() {
        List<T> list = entries().flatMap(entry -> entry.getValue().stream()).toList();
        IdMapper<T> idMapper = idMapper();
        if (idMapper == null) {
            idMapper = new IdMapper<>(list.size());
        }
        for (T provider : list) {
            if (idMapper.getId(provider) == -1) {
                idMapper.add(provider);
            }
        }
        return idMapper;
    }

    default void remapIds(List<ResourceLocation> ids) {
        IdMapper<T> idMapper = Objects.requireNonNull(idMapper());
        Map<ResourceLocation, T> map = Streams.stream(idMapper).collect(Collectors.toMap(IJadeProvider::getUid, Function.identity()));
        int i = 0;
        for (ResourceLocation id : ids) {
            T object = map.get(id);
            if (object != null) {
                idMapper.addMapping(object, i);
            }
            i++;
        }
    }
}

