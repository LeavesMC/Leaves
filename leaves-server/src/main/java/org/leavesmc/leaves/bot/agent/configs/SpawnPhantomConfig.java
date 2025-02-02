package org.leavesmc.leaves.bot.agent.configs;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.agent.BotConfig;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.List;

public class SpawnPhantomConfig extends BotConfig<Boolean> {

    private boolean value;

    public SpawnPhantomConfig() {
        super("spawn_phantom", CommandArgument.of(CommandArgumentType.BOOLEAN).setTabComplete(0, List.of("ture", "false")), SpawnPhantomConfig::new);
        this.value = LeavesConfig.modify.fakeplayer.canSpawnPhantom;
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
    public List<String> getMessage() {
        return List.of(
                bot.getScoreboardName() + "'s spawn_phantom: " + this.getValue(),
                bot.getScoreboardName() + "'s not_sleeping_ticks: " + bot.notSleepTicks
        );
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putBoolean("spawn_phantom", this.getValue());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.value = nbt.getBoolean("spawn_phantom");
    }
}
