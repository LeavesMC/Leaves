package org.leavesmc.leaves.bot.agent.actions;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.LeavesWrappedArgument;
import org.leavesmc.leaves.util.ExtraData;
import org.leavesmc.leaves.command.LeavesCommandContext;
import org.leavesmc.leaves.entity.bot.action.CustomBotAction;
import org.leavesmc.leaves.entity.bot.actions.CraftCustomBotAction;

public class ServerCustomBotAction extends AbstractBotAction<ServerCustomBotAction> {
    private final CustomBotAction customBotAction;

    public ServerCustomBotAction(@NotNull CustomBotAction customBotAction) {
        super(customBotAction.getName(), () -> new ServerCustomBotAction(customBotAction));
        this.customBotAction = customBotAction;
        customBotAction.provideArgumentFactory(new LeavesWrappedArgument.ArgumentHandler() {
            @Override
            public <T> LeavesWrappedArgument<T> create(String name, ArgumentType<T> type) {
                return addArgument(name, type);
            }

            @Override
            public void fork(int forkId) {
                ServerCustomBotAction.this.fork(forkId);
            }
        });
        customBotAction.init();
    }

    @Override
    public String getActionDataString(@NotNull ExtraData data) {
        customBotAction.provideActionData(data);
        return super.getActionDataString(data);
    }

    @Override
    public void loadCommand(@NotNull LeavesCommandContext context) throws CommandSyntaxException {
        super.loadCommand(context);
        customBotAction.loadCommand(context.rawContext());
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return customBotAction.doTick(bot.getBukkitEntity());
    }

    @Override
    public Object asCraft() {
        return new CraftCustomBotAction(this);
    }
}
