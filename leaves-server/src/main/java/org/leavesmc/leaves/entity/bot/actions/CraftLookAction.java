package org.leavesmc.leaves.entity.bot.actions;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.leavesmc.leaves.bot.agent.actions.ServerLookAction;
import org.leavesmc.leaves.entity.bot.action.LookAction;

public class CraftLookAction extends CraftBotAction<LookAction, ServerLookAction> implements LookAction {

    public CraftLookAction(ServerLookAction serverAction) {
        super(serverAction, CraftLookAction::new);
    }

    @Override
    public LookAction setPos(Vector pos) {
        serverAction.setPos(pos);
        return this;
    }

    @Override
    public Vector getPos() {
        return serverAction.getPos();
    }

    @Override
    public LookAction setTarget(Player player) {
        serverAction.setTarget(((CraftPlayer) player).getHandle());
        return this;
    }

    @Override
    public Player getTarget() {
        return serverAction.getTarget().getBukkitEntity();
    }
}