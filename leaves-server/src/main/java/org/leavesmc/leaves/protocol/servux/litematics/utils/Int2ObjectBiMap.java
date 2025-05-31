package org.leavesmc.leaves.protocol.servux.litematics.utils;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;

public class Int2ObjectBiMap<K> implements Iterable<K> {

    private static final Object EMPTY = null;
    private K[] values;
    private int[] ids;
    private K[] idToValues;
    private int nextId;
    private int size;

    @SuppressWarnings("unchecked")
    private Int2ObjectBiMap(int size) {
        this.values = (K[]) (new Object[size]);
        this.ids = new int[size];
        this.idToValues = (K[]) (new Object[size]);
    }

    private Int2ObjectBiMap(K[] values, int[] ids, K[] idToValues, int nextId, int size) {
        this.values = values;
        this.ids = ids;
        this.idToValues = idToValues;
        this.nextId = nextId;
        this.size = size;
    }

    public static <A> Int2ObjectBiMap<A> create(int expectedSize) {
        return new Int2ObjectBiMap<>((int) ((float) expectedSize / 0.8F));
    }

    private static int idealHash(int value) {
        value ^= value >>> 16;
        value *= -2048144789;
        value ^= value >>> 13;
        value *= -1028477387;
        return value ^ value >>> 16;
    }

    public int getRawId(@Nullable K value) {
        return this.getIdFromIndex(this.findIndex(value, this.getIdealIndex(value)));
    }

    @Nullable
    public K get(int index) {
        return index >= 0 && index < this.idToValues.length ? this.idToValues[index] : null;
    }

    private int getIdFromIndex(int index) {
        return index == -1 ? -1 : this.ids[index];
    }

    public boolean contains(K value) {
        return this.getRawId(value) != -1;
    }

    public boolean containsKey(int index) {
        return this.get(index) != null;
    }

    public int add(K value) {
        int i = this.nextId();
        this.put(value, i);
        return i;
    }

    private int nextId() {
        while (this.nextId < this.idToValues.length && this.idToValues[this.nextId] != null) {
            this.nextId++;
        }

        return this.nextId;
    }

    private void resize(int newSize) {
        K[] objects = this.values;
        int[] is = this.ids;
        Int2ObjectBiMap<K> int2ObjectBiMap = new Int2ObjectBiMap<>(newSize);

        for (int i = 0; i < objects.length; i++) {
            if (objects[i] != null) {
                int2ObjectBiMap.put(objects[i], is[i]);
            }
        }

        this.values = int2ObjectBiMap.values;
        this.ids = int2ObjectBiMap.ids;
        this.idToValues = int2ObjectBiMap.idToValues;
        this.nextId = int2ObjectBiMap.nextId;
        this.size = int2ObjectBiMap.size;
    }

    public void put(K value, int id) {
        int i = Math.max(id, this.size + 1);
        if ((float) i >= (float) this.values.length * 0.8F) {
            int j = this.values.length << 1;

            while (j < id) {
                j <<= 1;
            }

            this.resize(j);
        }

        int j = this.findFree(this.getIdealIndex(value));
        this.values[j] = value;
        this.ids[j] = id;
        this.idToValues[id] = value;
        this.size++;
        if (id == this.nextId) {
            this.nextId++;
        }
    }

    private int getIdealIndex(@Nullable K value) {

        return (idealHash(System.identityHashCode(value)) & 2147483647) % this.values.length;
    }

    private int findIndex(@Nullable K value, int id) {
        for (int i = id; i < this.values.length; i++) {
            if (this.values[i] == value) {
                return i;
            }

            if (this.values[i] == EMPTY) {
                return -1;
            }
        }

        for (int i = 0; i < id; i++) {
            if (this.values[i] == value) {
                return i;
            }

            if (this.values[i] == EMPTY) {
                return -1;
            }
        }

        return -1;
    }

    private int findFree(int size) {
        for (int i = size; i < this.values.length; i++) {
            if (this.values[i] == EMPTY) {
                return i;
            }
        }

        for (int ix = 0; ix < size; ix++) {
            if (this.values[ix] == EMPTY) {
                return ix;
            }
        }

        throw new RuntimeException("Overflowed :(");
    }

    public @NotNull Iterator<K> iterator() {
        return Iterators.filter(Iterators.forArray(this.idToValues), Predicates.notNull());
    }

    public void clear() {
        Arrays.fill(this.values, null);
        Arrays.fill(this.idToValues, null);
        this.nextId = 0;
        this.size = 0;
    }

    public int size() {
        return this.size;
    }

    public Int2ObjectBiMap<K> copy() {
        return new Int2ObjectBiMap<>(this.values.clone(), this.ids.clone(), this.idToValues.clone(), this.nextId, this.size);
    }

    public K getOrThrow(int index) {
        K object = this.get(index);
        if (object == null) {
            throw new IllegalArgumentException("No value with id " + index);
        } else {
            return object;
        }
    }
}