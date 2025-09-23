package org.leavesmc.leaves.bot.agent.actions;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.ExtraData;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.entity.bot.action.MoveAction.MoveDirection;
import org.leavesmc.leaves.entity.bot.actions.CraftMoveAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.leavesmc.leaves.command.ArgumentNode.ArgumentSuggestions.strings;

public class ServerMoveAction extends AbstractStateBotAction<ServerMoveAction> {
    private static final Map<String, MoveDirection> NAME_TO_DIRECTION = Arrays.stream(MoveDirection.values()).collect(toMap(
        it -> it.name,
        it -> it
    ));
    private MoveDirection direction = MoveDirection.FORWARD;

    public ServerMoveAction() {
        super("move", ServerMoveAction::new);
        this.addArgument("direction", StringArgumentType.word())
            .suggests(strings(Arrays.stream(MoveDirection.values()).map((it) -> it.name).toList()));
    }

    @Override
    public void loadCommand(@NotNull CommandContext context) throws CommandSyntaxException {
        String raw = context.getArgument("direction", String.class);
        MoveDirection direction = NAME_TO_DIRECTION.get(raw);
        if (direction == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        }
        this.direction = direction;
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

    @Override
    public String getActionDataString(@NotNull ExtraData data) {
        data.add("direction", direction.name);
        return super.getActionDataString(data);
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