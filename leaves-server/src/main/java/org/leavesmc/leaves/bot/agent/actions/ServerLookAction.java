package org.leavesmc.leaves.bot.agent.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.entity.bot.actions.CraftLookAction;

public class ServerLookAction extends AbstractBotAction<ServerLookAction> {

    private static final Vector ZERO_VECTOR = new Vector(0, 0, 0);

    private Vector pos = ZERO_VECTOR;
    private ServerPlayer target = null;

    public ServerLookAction() {
        super("look", ServerLookAction::new);
        this.addArgument("player", EntityArgument.player()).setOptional(true);
        this.fork(1);
        this.addArgument("location", Vec3Argument.vec3(false));
    }

    @Override
    public void loadCommand(@NotNull CommandContext context) throws CommandSyntaxException {
        EntitySelector selector = context.getArgumentOrDefault("player", EntitySelector.class, null);
        Coordinates location = context.getArgumentOrDefault("location", Coordinates.class, null);
        CommandSourceStack source = context.getSource();
        if (selector == null && location == null) {
            Entity sender = source.getEntityOrException();
            this.setPos(new Vector(sender.getX(), sender.getY(), sender.getZ()));
        } else if (selector != null) {
            ServerPlayer player = selector.findSinglePlayer(source);
            this.setTarget(player);
        } else {
            Vec3 vector = location.getPosition(source);
            this.setPos(new Vector(vector.x, vector.y, vector.z));
        }
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag nbt) {
        super.save(nbt);
        if (target != null) {
            this.pos.setX(this.target.getX());
            this.pos.setY(this.target.getY());
            this.pos.setZ(this.target.getZ());
        }
        nbt.putDouble("x", this.pos.getX());
        nbt.putDouble("y", this.pos.getY());
        nbt.putDouble("z", this.pos.getZ());
        return nbt;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.setPos(new Vector(
            nbt.getDouble("x").orElse(0.0),
            nbt.getDouble("y").orElse(0.0),
            nbt.getDouble("z").orElse(0.0)
        ));
    }

    public void setPos(Vector pos) {
        this.pos = pos;
    }

    public Vector getPos() {
        return this.pos;
    }

    public void setTarget(ServerPlayer player) {
        this.target = player;
    }

    public ServerPlayer getTarget() {
        return target;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (target != null) {
            bot.faceLocation(target.getBukkitEntity().getLocation());
        } else {
            bot.look(pos.subtract(bot.getLocation().toVector()), false);
        }
        return true;
    }

    @Override
    public Object asCraft() {
        return new CraftLookAction(this);
    }
}