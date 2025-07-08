package org.leavesmc.leaves.entity.bot.action;

public interface CustomStateBotAction extends StateBotAction<CustomStateBotAction> {

    /**
     * Executes the action, returning its success.
     *
     * @param bot bot of the action
     * @return true if once action finish, otherwise false
     */
    boolean doTick(org.leavesmc.leaves.entity.bot.Bot bot);

    /**
     * Created a new action instance.
     *
     * @param player player who create this action
     * @param args   passed action arguments
     * @return a new action instance with given args
     */
    @org.jetbrains.annotations.Nullable CustomStateBotAction getNew(@org.jetbrains.annotations.Nullable org.bukkit.entity.Player player, String[] args);

    /**
     * Requests a list of possible completions for a action argument.
     *
     * @return A List of a List of possible completions for the argument.
     */
    @org.jetbrains.annotations.NotNull java.util.List<java.util.List<String>> getTabComplete();
}
