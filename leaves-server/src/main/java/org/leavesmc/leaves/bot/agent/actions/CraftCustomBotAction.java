package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.action.CustomBotAction;

import java.lang.reflect.InvocationTargetException;

public class CraftCustomBotAction extends CraftBotAction<CustomBotAction> implements CraftCustomAction<CraftCustomBotAction> {

    private final CustomBotAction realAction;

    public CraftCustomBotAction(String name, @NotNull CustomBotAction realAction) {
        super(name, CommandArgument.EMPTY, null);
        this.realAction = realAction;
    }

    @Override
    public CraftCustomBotAction createCraft(@Nullable Player player, String[] args) {
        CustomBotAction newRealAction = realAction.fromCommand(player, args);
        if (newRealAction != null) {
            return new CraftCustomBotAction(this.getName(), newRealAction);
        }
        return null;
    }

    @Override
    public CraftCustomBotAction createEmptyCraft() {
        try {
            CustomBotAction newRealAction = realAction.getClass().getConstructor().newInstance();
            return new CraftCustomBotAction(this.getName(), newRealAction);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return realAction.doTick(bot.getBukkitEntity());
    }

    @Override
    public Class<CustomBotAction> getInterfaceClass() {
        return null;
    }
}
