package org.leavesmc.leaves.bot.agent.configs;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.CommandContext;

import static net.minecraft.network.chat.Component.literal;

public class SimulationDistanceConfig extends AbstractBotConfig<Integer> {

    public SimulationDistanceConfig() {
        super("simulation_distance", IntegerArgumentType.integer(2, 32));
    }

    @Override
    public void applySuggestions(CommandContext context, @NotNull SuggestionsBuilder builder) {
        builder.suggest("2", literal("Minimum simulation distance"));
        builder.suggest("8");
        builder.suggest("12");
        builder.suggest("16");
        builder.suggest("32", literal("Maximum simulation distance"));
    }

    @Override
    public Integer getValue() {
        return this.bot.getBukkitEntity().getSimulationDistance();
    }

    @Override
    public void setValue(Integer value) {
        this.bot.getBukkitEntity().setSimulationDistance(value);
    }

    @Override
    public Integer loadFromCommand(@NotNull CommandContext context) {
        return context.getInteger(getName());
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putInt(getName(), this.getValue());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.setValue(nbt.getIntOr(getName(), LeavesConfig.modify.fakeplayer.inGame.getSimulationDistance(this.bot)));
    }
}
