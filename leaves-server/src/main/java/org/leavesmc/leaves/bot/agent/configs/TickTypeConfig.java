package org.leavesmc.leaves.bot.agent.configs;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandContext;

public class TickTypeConfig extends AbstractBotConfig<ServerBot.TickType, String, TickTypeConfig> {
    private ServerBot.TickType value;

    public TickTypeConfig() {
        super("tick_type", StringArgumentType.word(), TickTypeConfig::new);
        this.value = LeavesConfig.modify.fakeplayer.inGame.tickType;
    }

    @Override
    public void applySuggestions(CommandContext context, @NotNull SuggestionsBuilder builder) {
        builder.suggest("network");
        builder.suggest("entity_list");
    }

    @Override
    public ServerBot.TickType loadFromCommand(@NotNull CommandContext context) throws CommandSyntaxException {
        String raw = context.getString(getName());
        return switch (raw) {
            case "network" -> ServerBot.TickType.NETWORK;
            case "entity_list" -> ServerBot.TickType.ENTITY_LIST;
            default -> throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        };
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
        nbt.putString(getName(), this.getValue().toString());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        String raw = nbt.getStringOr(getName(), LeavesConfig.modify.fakeplayer.inGame.tickType.name());
        this.setValue(switch (raw) {
            case "network" -> ServerBot.TickType.NETWORK;
            case "entity_list" -> ServerBot.TickType.ENTITY_LIST;
            default -> throw new IllegalStateException("Unexpected bot tick type value: " + raw);
        });
    }
}
