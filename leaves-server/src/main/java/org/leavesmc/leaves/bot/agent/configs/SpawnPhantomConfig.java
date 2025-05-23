package org.leavesmc.leaves.bot.agent.configs;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.agent.AbstractBotConfig;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.List;

public class SpawnPhantomConfig extends AbstractBotConfig<Boolean> {

    public static final String NAME = "spawn_phantom";

    private boolean value;

    public SpawnPhantomConfig() {
        super(NAME, CommandArgument.of(CommandArgumentType.BOOLEAN).setSuggestion(0, List.of("true", "false")));
        this.value = LeavesConfig.modify.fakeplayer.canSpawnPhantom;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean value) throws IllegalArgumentException {
        this.value = value;
    }

    @Override
    public List<String> getMessage() {
        return List.of(
            bot.getScoreboardName() + "'s spawn_phantom: " + this.getValue(),
            bot.getScoreboardName() + "'s not_sleeping_ticks: " + bot.notSleepTicks
        );
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putBoolean(NAME, this.getValue());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.setValue(nbt.getBoolean(NAME).orElseThrow());
    }
}
