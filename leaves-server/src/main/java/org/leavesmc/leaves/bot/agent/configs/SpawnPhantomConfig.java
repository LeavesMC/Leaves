package org.leavesmc.leaves.bot.agent.configs;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.CommandContext;

import java.util.List;

public class SpawnPhantomConfig extends AbstractBotConfig<Boolean, Boolean, SpawnPhantomConfig> {
    private boolean value;

    public SpawnPhantomConfig() {
        super("spawn_phantom", BoolArgumentType.bool(), SpawnPhantomConfig::new);
        this.value = LeavesConfig.modify.fakeplayer.inGame.canSpawnPhantom;
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
    public List<Pair<String, String>> getExtraData() {
        return List.of(Pair.of("not_sleeping_ticks", String.valueOf(bot.notSleepTicks)));
    }

    @Override
    public Boolean loadFromCommand(@NotNull CommandContext context) {
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
        this.setValue(nbt.getBooleanOr(getName(), LeavesConfig.modify.fakeplayer.inGame.canSpawnPhantom));
    }
}
