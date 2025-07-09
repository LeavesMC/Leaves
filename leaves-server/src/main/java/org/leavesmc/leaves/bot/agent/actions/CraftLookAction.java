package org.leavesmc.leaves.bot.agent.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandArgument;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.CommandArgumentType;
import org.leavesmc.leaves.entity.bot.action.LookAction;

import java.text.DecimalFormat;
import java.util.List;

public class CraftLookAction extends CraftBotAction<LookAction> implements LookAction {

    private static final DecimalFormat DF = new DecimalFormat("0.0");
    private static final Vector ZERO_VECTOR = new Vector(0, 0, 0);

    public CraftLookAction() {
        super("look", CommandArgument.of(CommandArgumentType.STRING, CommandArgumentType.DOUBLE, CommandArgumentType.DOUBLE), CraftLookAction::new);
        this.setSuggestion(0, (sender, arg) -> sender instanceof ServerPlayer player ? Pair.of(List.of(DF.format(player.getX())), "<X>/<Player>") : Pair.of(List.of("0"), "<X>/<Player>"));
        this.setSuggestion(1, (sender, arg) -> sender instanceof ServerPlayer player ? Pair.of(List.of(DF.format(player.getY())), "<Y>") : Pair.of(List.of("0"), "<Y>"));
        this.setSuggestion(2, (sender, arg) -> sender instanceof ServerPlayer player ? Pair.of(List.of(DF.format(player.getZ())), "<Z>") : Pair.of(List.of("0"), "<Z>"));
    }

    @Override
    public @NotNull Class<LookAction> getActionRegClass() {
        return LookAction.class;
    }

    private Vector pos = ZERO_VECTOR;
    private Player target = null;

    @Override
    public void loadCommand(ServerPlayer player, @NotNull CommandArgumentResult result) throws IllegalArgumentException {
        Object rawFirst = result.readObject();
        if (rawFirst == null) {
            this.target = player.getBukkitEntity();
            return;
        }
        Double second = result.read(Double.class);
        if (second == null) {
            this.target = Bukkit.getPlayer(rawFirst.toString());
            if (this.target == null) throw new IllegalArgumentException("Player not found: " + rawFirst);
            return;
        }
        Double third = result.read(Double.class);
        if (third == null) {
            throw new IllegalArgumentException("Missing Z coordinate for look action.");
        }
        double first;
        try {
            first = Double.parseDouble(rawFirst.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for X: " + rawFirst, e);
        }
        this.pos = new Vector(
            first,
            second,
            third
        );
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
        this.setPos(
            new Vector(
                nbt.getDouble("x").orElse(0.0),
                nbt.getDouble("y").orElse(0.0),
                nbt.getDouble("z").orElse(0.0)
            )
        );
    }

    @Override
    public LookAction setPos(Vector pos) {
        this.pos = pos;
        return this;
    }

    @Override
    public Vector getPos() {
        return this.pos;
    }

    @Override
    public LookAction setTarget(Player player) {
        this.target = player;
        return this;
    }

    @Override
    public Player getTarget() {
        return target;
    }

    @Override
    public boolean doTick(@NotNull ServerBot bot) {
        if (target != null) bot.faceLocation(target.getLocation());
        else bot.look(pos.subtract(bot.getLocation().toVector()), false);
        return true;
    }
}
