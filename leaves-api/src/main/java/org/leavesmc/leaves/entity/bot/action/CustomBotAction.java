package org.leavesmc.leaves.entity.bot.action;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.bot.Bot;

import java.util.List;


/**
 * Represents a class which contains methods for a custom bot action
 */
public interface CustomBotAction {

    /**
     * Executes the action, returning its success.
     *
     * @param bot bot of the action
     * @return true if once action finish, otherwise false
     */
    boolean doTick(Bot bot);

    /**
     * Loads the action with given arguments.
     *
     * @param player player who create this action
     * @param args   passed action arguments
     */
    void loadCommand(@Nullable Player player, String[] args);

    /**
     * Requests a list of possible completions for a action argument.
     *
     * @return A List of a List of possible completions for the argument.
     */
    @NotNull List<List<String>> getTabComplete();
}
