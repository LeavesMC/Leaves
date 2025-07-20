package org.leavesmc.leaves.protocol.servux.logger;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.protocol.servux.logger.data.MobCapData;
import org.leavesmc.leaves.protocol.servux.logger.data.TPSData;

import java.util.function.Function;

public abstract class DataLogger<T> {

    private final Type type;

    public DataLogger(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public abstract T getResult(MinecraftServer server);

    public enum Type implements StringRepresentable {
        TPS("tps", Tps::new, Tps.CODEC),
        MOB_CAPS("mob_caps", MobCaps::new, MobCaps.CODEC);

        public static final StringRepresentable.EnumCodec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        public static final ImmutableList<Type> VALUES = ImmutableList.copyOf(values());

        private final String name;
        private final Function<Type, DataLogger<?>> factory;
        private final Codec<?> codec;

        Type(String name, Function<Type, DataLogger<?>> factory, Codec<?> codec) {
            this.name = name;
            this.factory = factory;
            this.codec = codec;
        }

        public static @Nullable Type fromStringStatic(String name) {
            for (Type type : VALUES) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type;
                }
            }

            return null;
        }

        public Function<Type, DataLogger<?>> getFactory() {
            return factory;
        }

        public @Nullable DataLogger<?> init() {
            return this.factory.apply(this);
        }

        public Codec<?> codec() {
            return this.codec;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    public static class Tps extends DataLogger<CompoundTag> {

        public static final Codec<CompoundTag> CODEC = CompoundTag.CODEC;

        public Tps(Type type) {
            super(type);
        }

        @Override
        public CompoundTag getResult(MinecraftServer server) {
            try {
                return (CompoundTag) TPSData.CODEC.encodeStart(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), this.build(server)).getOrThrow();
            } catch (Exception e) {
                return new CompoundTag();
            }
        }

        private TPSData build(MinecraftServer server) {
            ServerTickRateManager tickManager = server.tickRateManager();
            boolean frozen = tickManager.isFrozen();
            boolean sprinting = tickManager.isSprinting();
            final double mspt = server.tickTimes5s.getAverage() / 1_000_000;
            double tps = 1000.0D / Math.max(sprinting ? 0.0 : tickManager.millisecondsPerTick(), mspt);

            if (frozen) {
                tps = 0.0d;
            }

            return new TPSData(
                mspt, tps,
                tickManager.getRemainingSprintTicks(),
                frozen, sprinting,
                tickManager.isSteppingForward()
            );
        }
    }

    public static class MobCaps extends DataLogger<CompoundTag> {

        public static final Codec<CompoundTag> CODEC = CompoundTag.CODEC;

        public MobCaps(Type type) {
            super(type);
        }

        @Override
        public CompoundTag getResult(MinecraftServer server) {
            CompoundTag nbt = new CompoundTag();

            for (ServerLevel world : server.getAllLevels()) {
                String dimKey = world.dimension().registry().toString();
                MobCapData mobCapData = new MobCapData();
                MobCapData.Cap[] data = MobCapData.createCapArray();
                NaturalSpawner.SpawnState info = world.getChunkSource().getLastSpawnState();
                int spawnableChunks = world.getChunkSource().chunkMap.getDistanceManager().getNaturalSpawnChunkCount();

                if (info == null || spawnableChunks <= 0) {
                    continue;
                }

                int divisor = 17 * 17;
                long worldTime = world.getGameTime();

                for (Object2IntMap.Entry<MobCategory> entry : info.getMobCategoryCounts().object2IntEntrySet()) {
                    int current = entry.getIntValue();
                    int capacity = entry.getKey().getMaxInstancesPerChunk() * spawnableChunks / divisor;
                    int ordinal = entry.getKey().ordinal();
                    data[ordinal].setCurrentAndCap(current, capacity);

                    MobCapData.Cap cap = data[ordinal];
                    mobCapData.setCurrentAndCapValues(entry.getKey(), cap.getCurrent(), cap.getCap(), worldTime);
                }

                try {
                    CompoundTag nbtEntry = (CompoundTag) MobCapData.CODEC.encodeStart(world.registryAccess().createSerializationContext(NbtOps.INSTANCE), mobCapData).getPartialOrThrow();
                    nbtEntry.putLong("WorldTick", worldTime);
                    nbt.put(dimKey, nbtEntry);
                } catch (Exception ignored) {
                }
            }

            return nbt;
        }
    }
}