package org.leavesmc.leaves.bot.agent.configs;

import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.agent.AbstractBotConfig;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.List;

public class AlwaysSendDataConfig extends AbstractBotConfig<Boolean> {

    public static final String NAME = "always_send_data";

    private boolean value;

    public AlwaysSendDataConfig() {
        super(NAME, CommandArgument.of(CommandArgumentType.BOOLEAN).setSuggestion(0, List.of("true", "false")));
        this.value = LeavesConfig.modify.fakeplayer.canSendDataAlways;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putBoolean(NAME, this.getValue());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.setValue(nbt.getBoolean(NAME).orElseThrow());
    }
}
