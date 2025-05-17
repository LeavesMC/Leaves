package org.leavesmc.leaves.bot.agent.configs;

import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.agent.AbstractBotConfig;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.List;

public class SimulationDistanceConfig extends AbstractBotConfig<Integer> {

    public static final String NAME = "simulation_distance";

    public SimulationDistanceConfig() {
        super(NAME, CommandArgument.of(CommandArgumentType.INTEGER).setSuggestion(0, Pair.of(List.of("2", "10"), "<INT 2 - 32>")));
    }

    @Override
    public Integer getValue() {
        return this.bot.getBukkitEntity().getSimulationDistance();
    }

    @Override
    public void setValue(Integer value) {
        if (value < 2 || value > 32) {
            throw new IllegalArgumentException("simulation_distance must be a number between 2 and 32, got: " + value);
        }
        this.bot.getBukkitEntity().setSimulationDistance(value);
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putInt(NAME, this.getValue());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.setValue(nbt.getInt(NAME).orElseThrow());
    }
}
