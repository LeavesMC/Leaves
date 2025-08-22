package org.leavesmc.leaves.bot.agent.actions;

import com.mojang.brigadier.arguments.StringArgumentType;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.action.MoveAction.MoveDirection;
import org.leavesmc.leaves.entity.bot.actions.CraftMoveAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;
import org.leavesmc.leaves.neo_command.CommandContext;

import java.util.Arrays;

import static org.leavesmc.leaves.neo_command.leaves.ArgumentSuggestions.strings;

public class ServerMoveAction extends ServerStateBotAction<ServerMoveAction> {

    private MoveDirection direction = MoveDirection.FORWARD;

    public ServerMoveAction() {
        super("move", ServerMoveAction::new);
        this.addArgument("direction", StringArgumentType.word())
            .suggests(strings(Arrays.stream(MoveDirection.values()).map(MoveDirection::name).toList()));
    }

    @Override
    public void loadCommand(@NotNull CommandContext context) {
        String raw = context.getArgument("direction", String.class);
        try {
            this.direction = MoveDirection.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid move direction: " + raw);
        }
    }

    @Override
    public void stop(@NotNull ServerBot bot, BotActionStopEvent.Reason reason) {
        super.stop(bot, reason);
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

    public MoveDirection getDirection() {
        return direction;
    }

    public void setDirection(MoveDirection direction) {
        this.direction = direction;
    }

    @Override
    public Object asCraft() {
        return new CraftMoveAction(this);
    }
}