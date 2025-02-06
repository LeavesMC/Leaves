package org.leavesmc.leaves.protocol.jade.provider.block;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;
import org.leavesmc.leaves.protocol.jade.accessor.BlockAccessor;
import org.leavesmc.leaves.protocol.jade.provider.StreamServerDataProvider;

public enum MobSpawnerCooldownProvider implements StreamServerDataProvider<BlockAccessor, Integer> {
    INSTANCE;

    private static final ResourceLocation MC_MOB_SPAWNER_COOLDOWN = JadeProtocol.mc_id("mob_spawner.cooldown");

    @Override
    public @Nullable Integer streamData(BlockAccessor accessor) {
        TrialSpawnerBlockEntity spawner = (TrialSpawnerBlockEntity) accessor.getBlockEntity();
        TrialSpawnerData spawnerData = spawner.getTrialSpawner().getData();
        ServerLevel level = ((ServerLevel) accessor.getLevel());
        if (spawner.getTrialSpawner().canSpawnInLevel(level) && level.getGameTime() < spawnerData.cooldownEndsAt) {
            return (int) (spawnerData.cooldownEndsAt - level.getGameTime());
        }
        return null;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Integer> streamCodec() {
        return ByteBufCodecs.VAR_INT.cast();
    }


    @Override
    public ResourceLocation getUid() {
        return MC_MOB_SPAWNER_COOLDOWN;
    }
}
