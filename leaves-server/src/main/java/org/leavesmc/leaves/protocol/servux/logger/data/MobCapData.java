package org.leavesmc.leaves.protocol.servux.logger.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class MobCapData {
    protected static final int CAP_COUNT = EntityCategory.values().length;

    public static final Codec<MobCapData> CODEC = RecordCodecBuilder.create(
        (inst) -> inst.group(
            PrimitiveCodec.INT.fieldOf("cap_count").forGetter(get -> CAP_COUNT),
            Codec.list(Cap.CODEC).fieldOf("cap_data").forGetter(get -> get.toList(get.data))
        ).apply(inst, MobCapData::new)
    );

    protected final Cap[] data;
    protected final Cap[] stagingData;
    protected final boolean[] dataValid;
    protected final long[] worldTicks;
    protected boolean hasValidData;
    protected long completionWorldTick;

    public MobCapData() {
        this.data = createCapArray();
        this.stagingData = createCapArray();
        this.dataValid = new boolean[CAP_COUNT];
        this.worldTicks = new long[CAP_COUNT];
    }

    private MobCapData(int capCount, List<Cap> capData) {
        this.data = this.createCapArrayFromList(capCount, capData);
        this.stagingData = this.createCapArray(capCount);
        this.dataValid = new boolean[capCount];
        this.worldTicks = new long[capCount];

        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = capData.get(i);
        }
    }

    public static Cap[] createCapArray() {
        Cap[] data = new Cap[CAP_COUNT];

        for (int i = 0; i < data.length; ++i) {
            data[i] = new Cap();
        }

        return data;
    }

    /**
     * @return The minimum value from the given array
     */
    public static long getMinValue(long[] arr) {
        if (arr.length == 0) {
            throw new IllegalArgumentException("Empty array");
        }

        final int size = arr.length;
        long minValue = arr[0];

        for (int i = 1; i < size; ++i) {
            if (arr[i] < minValue) {
                minValue = arr[i];
            }
        }

        return minValue;
    }

    /**
     * @return The maximum value from the given array
     */
    public static long getMaxValue(long[] arr) {
        if (arr.length == 0) {
            throw new IllegalArgumentException("Empty array");
        }

        final int size = arr.length;
        long maxValue = arr[0];

        for (int i = 1; i < size; ++i) {
            if (arr[i] > maxValue) {
                maxValue = arr[i];
            }
        }

        return maxValue;
    }

    private Cap[] createCapArrayFromList(final int size, List<Cap> list) {
        Cap[] data = new Cap[size];

        for (int i = 0; i < data.length; ++i) {
            data[i] = list.get(i);
        }

        return data;
    }

    private Cap[] createCapArray(final int size) {
        Cap[] data = new Cap[size];

        for (int i = 0; i < data.length; ++i) {
            data[i] = new Cap();
        }

        return data;
    }

    private List<Cap> toList(Cap[] caps) {
        return Arrays.stream(caps).toList();
    }

    public void clear() {
        this.clearStaging();
        this.hasValidData = false;
        this.completionWorldTick = -1L;
    }

    public Cap getCap(EntityCategory type) {
        return this.data[type.ordinal()];
    }

    public void setCurrentAndCapValues(EntityCategory type, int currentValue, int capValue, long worldTick) {
        int index = type.ordinal();

        this.stagingData[index].setCurrentAndCap(currentValue, capValue);
        this.dataValid[index] = true;
        this.worldTicks[index] = worldTick;

        this.checkStagingComplete(worldTick);
    }

    protected void clearStaging() {
        for (Cap data : this.stagingData) {
            data.setCurrentAndCap(-1, -1);
        }

        Arrays.fill(this.dataValid, false);
        Arrays.fill(this.worldTicks, -1);
    }

    protected void checkStagingComplete(long worldTick) {
        for (boolean b : this.dataValid) {
            if (!b) {
                return;
            }
        }

        long min = getMinValue(this.worldTicks);
        long max = getMaxValue(this.worldTicks);

        // Require all the values to have been received within 60 ticks
        // of each other for the data set to be considered valid
        if (max - min <= 60) {
            for (int i = 0; i < this.stagingData.length; ++i) {
                this.data[i].setFrom(this.stagingData[i]);
            }

            this.clearStaging();
        }

        this.hasValidData = true;
        this.completionWorldTick = worldTick;
    }

    public enum EntityCategory implements StringRepresentable {
        MONSTER("monster", MobCategory.MONSTER),
        CREATURE("creature", MobCategory.CREATURE),
        AMBIENT("ambient", MobCategory.AMBIENT),
        AXOLOTLS("axolotls", MobCategory.AXOLOTLS),
        UNDERGROUND_WATER_CREATURE("underground_water_creature", MobCategory.UNDERGROUND_WATER_CREATURE),
        WATER_CREATURE("water_creature", MobCategory.WATER_CREATURE),
        WATER_AMBIENT("water_ambient", MobCategory.WATER_AMBIENT),
        MISC("misc", MobCategory.MISC);

        public static final StringRepresentable.EnumCodec<EntityCategory> CODEC = StringRepresentable.fromEnum(EntityCategory::values);
        public static final ImmutableList<EntityCategory> VALUES = ImmutableList.copyOf(values());

        private final MobCategory vanillaCategory;
        private final String name;

        EntityCategory(String name, MobCategory vanillaCategory) {
            this.name = name;
            this.vanillaCategory = vanillaCategory;
        }

        public static EntityCategory fromVanillaCategory(MobCategory type) {
            return switch (type) {
                case MONSTER -> MONSTER;
                case CREATURE -> CREATURE;
                case AMBIENT -> AMBIENT;
                case AXOLOTLS -> AXOLOTLS;
                case UNDERGROUND_WATER_CREATURE -> UNDERGROUND_WATER_CREATURE;
                case WATER_CREATURE -> WATER_CREATURE;
                case WATER_AMBIENT -> WATER_AMBIENT;
                case MISC -> MISC;
            };
        }

        @Nullable
        public static EntityCategory fromVanillaCategoryName(String name) {
            return switch (name) {
                case "monster" -> MONSTER;
                case "creature" -> CREATURE;
                case "ambient" -> AMBIENT;
                case "axolotls" -> AXOLOTLS;
                case "underground_water_creature" -> UNDERGROUND_WATER_CREATURE;
                case "water_creature" -> WATER_CREATURE;
                case "water_ambient" -> WATER_AMBIENT;
                case "misc" -> MISC;
                default -> null;
            };
        }

        public String getName() {
            return this.name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }

        public MobCategory getVanillaCategory() {
            return this.vanillaCategory;
        }
    }

    public static class Cap {
        public static Codec<Cap> CODEC = RecordCodecBuilder.create(
            (inst) -> inst.group(
                PrimitiveCodec.INT.fieldOf("current").forGetter(Cap::getCurrent),
                PrimitiveCodec.INT.fieldOf("cap").forGetter(Cap::getCap)
            ).apply(inst, Cap::new));

        protected int current;
        protected int cap;

        public Cap() {
        }

        private Cap(int current, int cap) {
            this.current = current;
            this.cap = cap;
        }

        public int getCurrent() {
            return this.current;
        }

        public void setCurrent(int current) {
            this.current = current;
        }

        public int getCap() {
            return this.cap;
        }

        public void setCap(int cap) {
            this.cap = cap;
        }

        public boolean isFull() {
            return this.current >= this.cap && this.cap >= 0;
        }

        public void setCurrentAndCap(int current, int cap) {
            this.current = current;
            this.cap = cap;
        }

        public void setFrom(Cap other) {
            this.current = other.current;
            this.cap = other.cap;
        }
    }
}