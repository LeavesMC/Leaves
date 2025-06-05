package org.leavesmc.leaves.bot.agent.configs;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.AbstractBotConfig;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentType;

import java.util.List;

public class TickTypeConfig extends AbstractBotConfig<ServerBot.TickType> {

    private static final String NAME = "tick_type";
    private static final CommandArgumentType<ServerBot.TickType> TICK_TYPE_ARGUMENT = CommandArgumentType.ofEnum(ServerBot.TickType.class);

    private ServerBot.TickType value;

    public TickTypeConfig() {
        super(NAME, CommandArgument.of(TICK_TYPE_ARGUMENT).setSuggestion(0, List.of("network", "entity_list")));
        this.value = LeavesConfig.modify.fakeplayer.tickType;
    }

    @Override
    public ServerBot.TickType getValue() {
        return value;
    }

    @Override
    public void setValue(ServerBot.TickType value) throws IllegalArgumentException {
        this.value = value;
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        nbt.putString(NAME, this.getValue().toString());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        this.setValue(TICK_TYPE_ARGUMENT.parse(nbt.getString(NAME).orElseThrow()));
    }
}
