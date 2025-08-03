package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftVehicle;
import org.bukkit.entity.Vehicle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.entity.bot.actions.CraftMountAction;

import java.util.Collection;

public class ServerMountAction extends ServerBotAction<ServerMountAction> {

    public ServerMountAction() {
        super("mount", CommandArgument.EMPTY, ServerMountAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Vehicle target = findNearestAvailableVehicle(bot);
        if (target == null) {
            return false;
        }

        return bot.startRiding(((CraftVehicle) target).getHandle(), false);
    }

    @Override
    public Object asCraft() {
        return new CraftMountAction(this);
    }

    public @Nullable Vehicle findNearestAvailableVehicle(@NotNull Entity sourceEntity) {
        Location center = sourceEntity.getBukkitEntity().getLocation();
        Collection<Vehicle> vehicles = center.getNearbyEntitiesByType(
            Vehicle.class,
            3,
            vehicle -> vehicleHasSeat(vehicle, sourceEntity) && (manhattanDistance(sourceEntity, ((CraftVehicle) vehicle).getHandle()) <= 2)
        );

        double minDistanceSqr = Double.MAX_VALUE;
        Vehicle nearestVehicle = null;

        for (Vehicle vehicle : vehicles) {
            double distanceSqr = center.distanceSquared(vehicle.getLocation());
            if (distanceSqr < minDistanceSqr) {
                minDistanceSqr = distanceSqr;
                nearestVehicle = vehicle;
            }
        }

        return nearestVehicle;
    }

    private boolean vehicleHasSeat(Vehicle vehicleEntity, Entity entity) {
        if (!(vehicleEntity instanceof Vehicle vehicle)) {
            return false;
        }

        return ((CraftVehicle) vehicle).getHandle().canAddPassengerPublic(entity);
    }

    private int manhattanDistance(@NotNull Entity entity1, @NotNull Entity entity2) {
        return Math.abs(entity1.getBlockX() - entity2.getBlockX()) +
            Math.abs(entity1.getBlockY() - entity2.getBlockY()) +
            Math.abs(entity1.getBlockZ() - entity2.getBlockZ());
    }
}
