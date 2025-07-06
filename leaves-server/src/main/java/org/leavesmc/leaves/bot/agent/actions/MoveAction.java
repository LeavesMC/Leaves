package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.AbstractBotAction;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.botaction.MoveDirection;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.Arrays;
import java.util.List;

public class MoveAction extends AbstractBotAction<MoveAction> {
    private static final Pair<List<String>, String> suggestions = Pair.of(
        Arrays.stream(MoveDirection.values()).map((it) -> it.name).toList(),
        "<Direction>"
    );
    private MoveDirection direction;

    public MoveAction() {
        super("move", CommandArgument.of(CommandArgumentType.ofEnum(MoveDirection.class)), MoveAction::new);
        this.setSuggestion(0, (sender, arg) -> suggestions);
    }

    @Override
    public void loadCommand(@Nullable ServerPlayer player, @NotNull CommandArgumentResult result) {
        direction = result.read(MoveDirection.class);
        if (direction == null) {
            throw new IllegalArgumentException("Invalid direction");
        }
        this.setInitialTickDelay(0).setInitialTickInterval(1).setInitialNumber(-1);
    }

    @Override
    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        switch (direction) {
            case FORWARD, BACKWARD -> bot.zza = 0.0f;
            case LEFT, RIGHT -> bot.xxa = 0.0f;
        }
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        boolean isSneaking = bot.isShiftKeyDown();
        float velocity = isSneaking ? 0.3f : 1.0f;
        switch (direction) {
            case FORWARD -> bot.zza = velocity;
            case BACKWARD -> bot.zza = -velocity;
            case LEFT -> bot.xxa = velocity;
            case RIGHT -> bot.xxa = -velocity;
        }
        return true;
    }
}
