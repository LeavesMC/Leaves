package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.BotAction;
import org.leavesmc.leaves.entity.botaction.BotActionType;
import org.leavesmc.leaves.entity.botaction.LeavesBotAction;

public class CraftBotAction extends LeavesBotAction {

    private final BotAction<?> handle;

    public CraftBotAction(@NotNull BotAction<?> action) {
        super(BotActionType.valueOf(action.getName()), action.getTickDelay(), action.getCanDoNumber());
        this.handle = action;
    }

    @Contract("_ -> new")
    @NotNull
    public static LeavesBotAction asAPICopy(BotAction<?> action) {
        return new CraftBotAction(action);
    }

    @NotNull
    public static BotAction<?> asInternalCopy(@NotNull LeavesBotAction action) {
        BotAction<?> act = Actions.getForName(action.getActionName());
        if (act == null) {
            throw new IllegalArgumentException("Invalid action name!");
        }

        BotAction<?> newAction = null;
        String[] args = new String[]{String.valueOf(action.getExecuteInterval()), String.valueOf(action.getRemainingExecuteTime())};
        try {
            if (act instanceof CraftCustomBotAction customBotAction) {
                newAction = customBotAction.createCraft(action.getActionPlayer(), args);
            } else {
                newAction = act.create();
                newAction.loadCommand(action.getActionPlayer() == null ? null : ((CraftPlayer) action.getActionPlayer()).getHandle(), act.getArgument().parse(0, args));
            }
        } catch (IllegalArgumentException ignore) {
        }

        if (newAction == null) {
            throw new IllegalArgumentException("Invalid action!"); // TODO look action
        }
        return newAction;
    }

    public BotAction<?> getHandle() {
        return handle;
    }
}
