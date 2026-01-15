package org.leavesmc.leaves.bot.agent.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
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
        super("look");
        this.addArgument("player", ArgumentTypes.player()).setOptional(true);
        this.fork(1);
        this.addArgument("location", ArgumentTypes.finePosition());
    }

    @Override
    public void loadCommand(@NotNull CommandContext context) throws CommandSyntaxException {
        PlayerSelectorArgumentResolver playerSelectorResolver = context.getArgumentOrDefault("player", PlayerSelectorArgumentResolver.class, null);
        FinePositionResolver positionResolver = context.getArgumentOrDefault("location", FinePositionResolver.class, null);
        CommandSourceStack source = context.getSource();
        if (playerSelectorResolver == null && positionResolver == null) {
            CommandSender sender = context.getSender();
            if (sender instanceof Entity entity) {
                this.setPos(entity.getLocation().toVector());
            } else {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create();
            }
        } else if (playerSelectorResolver != null) {
            CraftPlayer player = (CraftPlayer) playerSelectorResolver.resolve(source).getFirst();
            this.setTarget(player.getHandle());
        } else {
            this.setPos(positionResolver.resolve(source).toVector());
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