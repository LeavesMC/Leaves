package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.action.MoveAction.MoveDirection;
import org.leavesmc.leaves.entity.bot.actions.CraftMoveAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.Arrays;
import java.util.List;

public class ServerMoveAction extends ServerStateBotAction<ServerMoveAction> {

    private static final Pair<List<String>, String> suggestions = Pair.of(
        Arrays.stream(MoveDirection.values()).map((it) -> it.name).toList(),
        "<Direction>"
    );
    private MoveDirection direction = MoveDirection.FORWARD;

    public ServerMoveAction() {
        super("move", CommandArgument.of(CommandArgumentType.ofEnum(MoveDirection.class)), ServerMoveAction::new);
        this.setSuggestion(0, suggestions);
    }

    @Override
    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) {
        this.direction = result.read(MoveDirection.class);
        if (direction == null) {
            throw new IllegalArgumentException("Invalid direction");
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