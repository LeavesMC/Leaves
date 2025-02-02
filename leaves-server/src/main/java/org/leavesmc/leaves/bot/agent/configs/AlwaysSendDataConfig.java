package org.leavesmc.leaves.bot.agent.configs;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;

import org.leavesmc.leaves.bot.agent.BotConfig;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.List;

public class AlwaysSendDataConfig extends BotConfig<Boolean> {

    private boolean value;

    public AlwaysSendDataConfig() {
        super("always_send_data", CommandArgument.of(CommandArgumentType.BOOLEAN).setTabComplete(0, List.of("ture", "false")), AlwaysSendDataConfig::new);
        this.value = LeavesConfig.modify.fakeplayer.canSendDataAlways;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(@NotNull CommandArgumentResult result) throws IllegalArgumentException {
        this.value = result.readBoolean(this.value);
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putBoolean("always_send_data", this.getValue());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.value = nbt.getBoolean("always_send_data");
    }
}
