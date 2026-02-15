package org.leavesmc.leaves.bot.agent.configs;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.CommandContext;

public class AlwaysSendDataConfig extends AbstractBotConfig<Boolean> {
    private boolean value;

    public AlwaysSendDataConfig() {
        super("always_send_data", BoolArgumentType.bool());
        this.value = LeavesConfig.modify.fakeplayer.inGame.canSendDataAlways;
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
    public Boolean loadFromCommand(@NotNull CommandContext context) {
        return context.getBoolean(getName());
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putBoolean(getName(), this.getValue());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.setValue(nbt.getBooleanOr(getName(), LeavesConfig.modify.fakeplayer.inGame.canSendDataAlways));
    }
}
