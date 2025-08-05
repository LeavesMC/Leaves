package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.actions.CraftLookAction;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ServerLookAction extends ServerBotAction<ServerLookAction> {

    private static final Vector ZERO_VECTOR = new Vector(0, 0, 0);
    private static final DecimalFormat DF = new DecimalFormat("0.0");

    private Vector pos = ZERO_VECTOR;
    private ServerPlayer target = null;

    public ServerLookAction() {
        super("look", CommandArgument.of(CommandArgumentType.STRING, CommandArgumentType.DOUBLE, CommandArgumentType.DOUBLE), ServerLookAction::new);
        this.setSuggestion(0, (sender, arg) -> sender instanceof Player player ?
            Pair.of(Stream.concat(Arrays.stream(MinecraftServer.getServer().getPlayerNames()), Stream.of(DF.format(player.getX()))).toList(), "<Player>|<X>") :
            Pair.of(Stream.concat(Arrays.stream(MinecraftServer.getServer().getPlayerNames()), Stream.of("0")).toList(), "<Player>|<X>")
        );
        this.setSuggestion(1, (sender, arg) -> sender instanceof Player player ? Pair.of(List.of(DF.format(player.getY())), "<Y>") : Pair.of(List.of("0"), "<Y>"));
        this.setSuggestion(2, (sender, arg) -> sender instanceof Player player ? Pair.of(List.of(DF.format(player.getZ())), "<Z>") : Pair.of(List.of("0"), "<Z>"));
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
    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) {
        String nameOrX = result.readString(player.getScoreboardName());
        ServerPlayer player1 = player.getServer().getPlayerList().getPlayerByName(nameOrX);
        if (player1 != null) {
            this.setTarget(player1);
            return;
        }
        try {
            Vector vector = result.readVectorYZ(Double.parseDouble(nameOrX));
            this.setPos(vector);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid vector");
        }
    }

    @Override
    public Object asCraft() {
        return new CraftLookAction(this);
    }
}