package org.leavesmc.leaves.protocol.jade.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class PriorityStore<K, V> {

    private final Object2IntMap<K> priorities = new Object2IntLinkedOpenHashMap<>();
    private final Function<V, K> keyGetter;
    private final ToIntFunction<V> defaultPriorityGetter;
    private ImmutableList<K> sortedList = ImmutableList.of();
    private BiFunction<PriorityStore<K, V>, Collection<K>, List<K>> sortingFunction = (store, allKeys) -> allKeys.stream()
        .sorted(Comparator.comparingInt(store::byKey))
        .toList();

    public PriorityStore(ToIntFunction<V> defaultPriorityGetter, Function<V, K> keyGetter) {
        this.defaultPriorityGetter = defaultPriorityGetter;
        this.keyGetter = keyGetter;
    }

    public void setSortingFunction(BiFunction<PriorityStore<K, V>, Collection<K>, List<K>> sortingFunction) {
        this.sortingFunction = sortingFunction;
    }

    public void put(V provider) {
        Objects.requireNonNull(provider);
        put(provider, defaultPriorityGetter.applyAsInt(provider));
    }

    public void put(V provider, int priority) {
        Objects.requireNonNull(provider);
        K uid = keyGetter.apply(provider);
        Objects.requireNonNull(uid);
        priorities.put(uid, priority);
    }

    public void putUnsafe(K key, int priority) {
        Objects.requireNonNull(key);
        priorities.put(key, priority);
    }

    public void sort(Set<K> extraKeys) {
        Set<K> allKeys = priorities.keySet();
        if (!extraKeys.isEmpty()) {
            allKeys = Sets.union(priorities.keySet(), extraKeys);
        }

        sortedList = ImmutableList.copyOf(sortingFunction.apply(this, allKeys));
    }

    public int byValue(V value) {
        return byKey(keyGetter.apply(value));
    }

    public int byKey(K id) {
        return priorities.getInt(id);
    }

    public ImmutableList<K> getSortedList() {
        return sortedList;
    }
}
