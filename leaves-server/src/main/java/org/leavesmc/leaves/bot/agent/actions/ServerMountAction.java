package org.leavesmc.leaves.bot.agent.actions;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftVehicle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.actions.CraftMountAction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ServerMountAction extends ServerBotAction<ServerMountAction> {

    public ServerMountAction() {
        super("mount", CommandArgument.EMPTY, ServerMountAction::new);
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        Entity targetVehicle = findNearestAvailableVehicleOptimized(bot.getBukkitEntity());
        if (targetVehicle == null) {
            return false;
        }
        targetVehicle.addPassenger(bot.getBukkitEntity());
        return true;
    }

    @Override
    public Object asCraft() {
        return new CraftMountAction(this);
    }

    public @Nullable Entity findNearestAvailableVehicleOptimized(@NotNull Entity sourceEntity) {
        World world = sourceEntity.getWorld();
        Location centerLocation = sourceEntity.getLocation();

        Entity vehicle = searchAtManhattanDistance(world, centerLocation, 0, sourceEntity);
        if (vehicle != null) {
            return vehicle;
        }

        vehicle = searchAtManhattanDistance(world, centerLocation, 1, sourceEntity);
        if (vehicle != null) {
            return vehicle;
        }

        return searchAtManhattanDistance(world, centerLocation, 2, sourceEntity);
    }

    private @Nullable Entity searchAtManhattanDistance(World world, Location center, int distance, Entity sourceEntity) {
        Set<Location> positions = generateManhattanPositions(center, distance);

        for (Location pos : positions) {
            Collection<Entity> entities = world.getNearbyEntitiesByType(
                    Vehicle.class,
                    pos,
                    0.5,
                    0.5,
                    0.5,
                    entity -> isVehicleAvailable(entity, sourceEntity)
            );

            if (!entities.isEmpty()) {
                return entities.iterator().next();
            }
        }
        return null;
    }

    private boolean isVehicleAvailable(Entity vehicleEntity, Entity entity) {
        if (!(vehicleEntity instanceof Vehicle vehicle)) {
            return false;
        }

        return ((CraftVehicle) vehicle).getHandle().canAddPassengerPublic(((CraftEntity) entity).getHandle());
    }

    private @NotNull Set<Location> generateManhattanPositions(Location center, int distance) {
        Set<Location> positions = new HashSet<>();

        if (distance == 0) {
            positions.add(center.clone());
            return positions;
        }

        for (int x = -distance; x <= distance; x++) {
            for (int y = -distance; y <= distance; y++) {
                for (int z = -distance; z <= distance; z++) {
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) == distance) {
                        positions.add(center.clone().add(x, y, z));
                    }
                }
            }
        }
        return positions;
    }
}
