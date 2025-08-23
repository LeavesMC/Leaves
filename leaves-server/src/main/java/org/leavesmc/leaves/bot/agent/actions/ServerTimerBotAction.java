package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.neo_command.CommandContext;

import java.util.function.Supplier;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static org.leavesmc.leaves.neo_command.leaves.ArgumentSuggestions.strings;

public abstract class ServerTimerBotAction<E extends ServerTimerBotAction<E>> extends ServerBotAction<E> {

    public ServerTimerBotAction(String name, Supplier<E> creator) {
        super(name, creator);
        this.addArgument("delay", integer(0))
            .suggests(strings("0", "5", "10", "20"))
            .setOptional(true);
        this.addArgument("interval", integer(0))
            .suggests(strings("20", "0", "5", "10"))
            .setOptional(true);
        this.addArgument("do_number", integer(-1))
            .suggests(((context, builder) -> builder.suggest("-1", Component.literal("do infinite times"))))
            .setOptional(true);
    }

    @Override
    public void loadCommand(@NotNull CommandContext context) {
        this.setStartDelayTick(context.getIntegerOrDefault("delay", 0));
        this.setDoIntervalTick(context.getIntegerOrDefault("interval", 20));
        this.setDoNumber(context.getIntegerOrDefault("do_number", 1));
    }

    @Override
    public void provideActionData(@NotNull ActionData data) {
        super.provideActionData(data);
        data.add("delay", String.valueOf(this.getStartDelayTick()));
        data.add("interval", String.valueOf(this.getDoIntervalTick()));
        data.add("do_number", String.valueOf(this.getDoNumber()));
        data.add("remaining_do_number", String.valueOf(this.getDoNumberRemaining()));
    }
}
