package org.leavesmc.leaves.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WrappedLegacyRandom implements RandomSource {

    private final Random random = new Random();
    private long seed = 0;

    @Override
    public @NotNull RandomSource fork() {
        return new WrappedLegacyRandom();
    }

    @Override
    public @NotNull PositionalRandomFactory forkPositional() {
        return new LegacyRandomSource.LegacyPositionalRandomFactory(this.seed);
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        this.random.setSeed(seed);
    }

    @Override
    public int nextInt() {
        return this.random.nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return this.random.nextInt(bound);
    }

    @Override
    public long nextLong() {
        return this.random.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return this.random.nextBoolean();
    }

    @Override
    public float nextFloat() {
        return this.random.nextFloat();
    }

    @Override
    public double nextDouble() {
        return this.random.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return this.random.nextGaussian();
    }
}
