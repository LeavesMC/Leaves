package org.leavesmc.leaves.bot.agent.configs;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.agent.AbstractBotConfig;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.ArrayList;
import java.util.List;

public class SimulationDistanceConfig extends AbstractBotConfig<Integer> {

    public SimulationDistanceConfig() {
        super("simulation_distance", CommandArgument.of(CommandArgumentType.INTEGER).setTabComplete(0, List.of("2", "10", "<INT 2 - 32>")), SimulationDistanceConfig::new);
    }

    @Override
    public Integer getValue() {
        return this.bot.getBukkitEntity().getSimulationDistance();
    }

    @Override
    public void setValue(@NotNull CommandArgumentResult result) throws IllegalArgumentException {
        int realValue = result.readInt(this.bot.getBukkitEntity().getSimulationDistance());
        if (realValue < 2 || realValue > 32) {
            throw new IllegalArgumentException("simulation_distance must be a number between 2 and 32, got: " + result);
        }
        this.bot.getBukkitEntity().setSimulationDistance(realValue);
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putInt("simulation_distance", this.getValue());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.setValue(new CommandArgumentResult(new ArrayList<>(){{
            add(nbt.getInt("simulation_distance"));
        }}));
    }
}
