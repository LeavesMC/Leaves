package org.leavesmc.leaves.bot.agent.configs;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.LeavesCommandContext;

public class SkipSleepConfig extends AbstractBotConfig<Boolean, SkipSleepConfig> {

    public SkipSleepConfig() {
        super("skip_sleep", BoolArgumentType.bool(), SkipSleepConfig::new);
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
    public Boolean loadFromCommand(@NotNull LeavesCommandContext context) {
        return context.getBoolean(getName());
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putBoolean(getName(), this.getValue());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.setValue(nbt.getBooleanOr(getName(), LeavesConfig.modify.fakeplayer.inGame.canSkipSleep));
    }
}
