package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Vehicle;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftMountAction;

import java.util.Comparator;
import java.util.List;

public class ServerMountAction extends AbstractBotAction<ServerMountAction> {

    public ServerMountAction() {
        super("mount", ServerMountAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Location center = bot.getBukkitEntity().getLocation();
        List<Vehicle> vehicles = center.getNearbyEntitiesByType(
            Vehicle.class,
            4,
            vehicle -> manhattanDistance(bot, ((CraftEntity) vehicle).getHandle()) <= 3
        ).stream().sorted(Comparator.comparingDouble(
            (vehicle) -> center.distanceSquared(vehicle.getLocation())
        )).toList();

        for (Vehicle vehicle : vehicles) {
            if (bot.startRiding(((CraftEntity) vehicle).getHandle(), false)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Object asCraft() {
        return new CraftMountAction(this);
    }

    private double manhattanDistance(@NotNull Entity entity1, @NotNull Entity entity2) {
        return Math.abs(entity1.getX() - entity2.getX()) +
            Math.abs(entity1.getY() - entity2.getY()) +
            Math.abs(entity1.getZ() - entity2.getZ());
    }
}
