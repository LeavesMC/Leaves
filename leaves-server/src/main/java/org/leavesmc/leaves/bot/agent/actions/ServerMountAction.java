package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftVehicle;
import org.bukkit.entity.Vehicle;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.entity.bot.actions.CraftMountAction;

import java.util.Comparator;
import java.util.List;

public class ServerMountAction extends AbstractBotAction<ServerMountAction> {

    public ServerMountAction() {
        super("mount");
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Location center = bot.getBukkitEntity().getLocation();
        List<Vehicle> vehicles = center.getNearbyEntitiesByType(
            Vehicle.class,
            bot.entityInteractionRange()
        ).stream().sorted(Comparator.comparingDouble(
            (vehicle) -> center.distanceSquared(vehicle.getLocation())
        )).toList();

        for (Vehicle vehicle : vehicles) {
            CraftVehicle craftVehicle = (CraftVehicle) vehicle;
            if (!bot.hasLineOfSight(craftVehicle.getHandle())) {
                continue;
            }
            if (bot.startRiding(craftVehicle.getHandle())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Object asCraft() {
        return new CraftMountAction(this);
    }
}
