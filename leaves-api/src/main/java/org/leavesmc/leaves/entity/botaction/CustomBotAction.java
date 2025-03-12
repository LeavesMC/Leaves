package org.leavesmc.leaves.entity.botaction;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.entity.Bot;

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
    public boolean doTick(Bot bot);

    /**
     *  Created a new action instance.
     *
     * @param player player who create this action
     * @param args passed action arguments
     * @return a new action instance with given args
     */
    @Nullable
    public CustomBotAction getNew(@Nullable Player player, String[] args);

    /**
     * Requests a list of possible completions for a action argument.
     *
     * @return A List of a List of possible completions for the argument.
     */
    @NotNull
    public List<List<String>> getTabComplete();

    /**
     * Return a ticks to wait between {@link CustomBotAction#doTick(Bot)}
     *
     * @return the ticks to wait between runs
     */
    public int getTickDelay();

    /**
     * Return a number of times {@link CustomBotAction#doTick(Bot)} can return true
     *
     * @return the number of times an action can be executed
     */
    public int getNumber();
}
