package org.leavesmc.leaves.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseCoralPlantTypeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.CoralClawFeature;
import net.minecraft.world.level.levelgen.feature.CoralFeature;
import net.minecraft.world.level.levelgen.feature.CoralMushroomFeature;
import net.minecraft.world.level.levelgen.feature.CoralTreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

// Powered by fabric-carpet/src/main/java/carpet/helpers/FertilizableCoral.java
public interface FertilizableCoral extends BonemealableBlock {

    boolean isEnabled();

    @Override
    default boolean isValidBonemealTarget(@NotNull LevelReader world, @NotNull BlockPos pos, @NotNull BlockState state) {
        return isEnabled() && state.getValue(BaseCoralPlantTypeBlock.WATERLOGGED) && world.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    default boolean isBonemealSuccess(@NotNull Level world, RandomSource random, @NotNull BlockPos pos, @NotNull BlockState state) {
        ((ServerLevel) world).sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.3, 0.5, 0.3, 0.0);
        return random.nextFloat() < 0.15D;
    }

    @Override
    default void performBonemeal(@NotNull ServerLevel worldIn, RandomSource random, @NotNull BlockPos pos, @NotNull BlockState blockUnder) {
        int variant = random.nextInt(3);
        CoralFeature coral = switch (variant) {
            case 0 -> new CoralClawFeature(NoneFeatureConfiguration.CODEC);
            case 1 -> new CoralTreeFeature(NoneFeatureConfiguration.CODEC);
            default -> new CoralMushroomFeature(NoneFeatureConfiguration.CODEC);
        };

        MapColor color = blockUnder.getMapColor(worldIn, pos);
        BlockState properBlock = blockUnder;
        HolderSet.Named<Block> coralBlocks = worldIn.registryAccess().lookupOrThrow(Registries.BLOCK).getOrThrow(BlockTags.CORAL_BLOCKS);
        for (Holder<Block> block : coralBlocks) {
            properBlock = block.value().defaultBlockState();
            if (properBlock.getMapColor(worldIn, pos) == color) {
                break;
            }
        }
        worldIn.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_NONE);

        if (!coral.placeFeature(worldIn, random, pos, properBlock)) {
            worldIn.setBlock(pos, blockUnder, 3);
        } else {
            if (worldIn.random.nextInt(10) == 0) {
                BlockPos randomPos = pos.offset(worldIn.random.nextInt(16) - 8, worldIn.random.nextInt(8), worldIn.random.nextInt(16) - 8);
                if (coralBlocks.contains(worldIn.getBlockState(randomPos).getBlockHolder())) {
                    worldIn.setBlock(randomPos, Blocks.WET_SPONGE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }
}
