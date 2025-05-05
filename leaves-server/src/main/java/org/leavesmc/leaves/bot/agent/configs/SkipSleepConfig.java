package org.leavesmc.leaves.bot.agent.configs;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.agent.AbstractBotConfig;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.List;

public class SkipSleepConfig extends AbstractBotConfig<Boolean> {

    public static final String NAME = "skip_sleep";

    public SkipSleepConfig() {
        super(NAME, CommandArgument.of(CommandArgumentType.BOOLEAN).setSuggestion(0, List.of("true", "false")));
    }

    @Override
    public Boolean getValue() {
        return bot.fauxSleeping;
    }

    @Override
    public void setValue(Boolean value) throws IllegalArgumentException {
        bot.fauxSleeping = value;
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
