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
import java.util.Comparator;
import java.util.List;

public class ServerMountAction extends ServerBotAction<ServerMountAction> {

    public ServerMountAction() {
        super("mount", CommandArgument.EMPTY, ServerMountAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        return tryRideNearestVehicle(bot);
    }

    @Override
    public Object asCraft() {
        return new CraftMountAction(this);
    }

    public boolean tryRideNearestVehicle(@NotNull Entity sourceEntity) {
        Location center = sourceEntity.getBukkitEntity().getLocation();
        Collection<Vehicle> nearbyVehicles = center.getNearbyEntitiesByType(
            Vehicle.class,
            3,
            vehicle -> manhattanDistance(sourceEntity, ((CraftVehicle) vehicle).getHandle()) <= 2
        );

        List<Vehicle> vehicles = nearbyVehicles.stream().sorted(Comparator.comparingDouble(
            (vehicle) -> center.distanceSquared(vehicle.getLocation())
        )).toList();

        for (Vehicle vehicle : vehicles) {
            if (sourceEntity.startRiding(((CraftVehicle) vehicle).getHandle(), false)) {
                return true;
            }
        }

        return false;
    }

    private double manhattanDistance(@NotNull Entity entity1, @NotNull Entity entity2) {
        return Math.abs(entity1.getX() - entity2.getX()) +
                Math.abs(entity1.getY() - entity2.getY()) +
                Math.abs(entity1.getZ() - entity2.getZ());
    }
}
