package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.agent.AbstractBotAction;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.entity.botaction.BotActionType;
import org.leavesmc.leaves.entity.botaction.LeavesBotAction;

public class CraftBotAction extends LeavesBotAction {

    private final AbstractBotAction<?> handle;

    public CraftBotAction(@NotNull AbstractBotAction<?> action) {
        super(BotActionType.valueOf(action.getName()), action.getTickDelay(), action.getCanDoNumber());
        this.handle = action;
    }

    @Contract("_ -> new")
    @NotNull
    public static LeavesBotAction asAPICopy(AbstractBotAction<?> action) {
        return new CraftBotAction(action);
    }

    @NotNull
    public static AbstractBotAction<?> asInternalCopy(@NotNull LeavesBotAction action) {
        AbstractBotAction<?> act = Actions.getForName(action.getActionName());
        if (act == null) {
            throw new IllegalArgumentException("Invalid action name!");
        }

        AbstractBotAction<?> newAction = null;
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

    public AbstractBotAction<?> getHandle() {
        return handle;
    }
}
