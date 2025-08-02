package org.leavesmc.leaves.entity.bot.actions;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.leavesmc.leaves.bot.agent.actions.*;
import org.leavesmc.leaves.entity.bot.action.*;

public class CraftLookOnAction extends CraftBotAction<LookAction, ServerLookAction.ON> implements LookAction {

    public CraftLookOnAction(ServerLookAction.ON serverAction) {
        super(serverAction, CraftLookOnAction::new);
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