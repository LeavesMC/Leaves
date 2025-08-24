package org.leavesmc.leaves.command.bot.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotCreateState;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.event.bot.BotCreateEvent;
import org.leavesmc.leaves.command.ArgumentNode;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.bot.BotSubcommand;

import static net.kyori.adventure.text.Component.text;
import static net.minecraft.commands.arguments.DimensionArgument.getDimension;

public class CreateCommand extends BotSubcommand {

    public CreateCommand() {
        super("create");
        children(NameArgument::new);
    }

    protected static boolean handleCreateCommand(@NotNull CommandContext context) throws CommandSyntaxException {
        CommandSender sender = context.getSender();

        String name = context.getArgument(NameArgument.class);
        if (!canCreate(sender, name)) {
            return false;
        }
        String skinName = context.getArgumentOrDefault(SkinNameArgument.class, name);

        World world;
        try {
            world = getDimension(context.getMojangContext(), "world").getWorld();
        } catch (IllegalArgumentException e) {
            if (!(sender instanceof Entity entity)) {
                sender.sendMessage(text("Must specify world and location when executed by console", NamedTextColor.RED));
                return false;
            }
            world = entity.getWorld();
        }

        Location location = Bukkit.getWorlds().getFirst().getSpawnLocation();
        Coordinates coords = context.getArgumentOrDefault(LocationArgument.class, null);
        if (coords != null) {
            Vec3 vec3 = coords.getPosition(context.getSource());
            location = new Location(world, vec3.x, vec3.y, vec3.z);
        } else if (sender instanceof Entity entity) {
            location = entity.getLocation();
        }

        BotCreateState
            .builder(name, location)
            .createReason(BotCreateEvent.CreateReason.COMMAND)
            .skinName(skinName)
            .creator(sender)
            .spawnWithSkin(null);

        return true;
    }

    private static boolean canCreate(CommandSender sender, @NotNull String name) {
        BotList botList = BotList.INSTANCE;
        if (!name.matches("^[a-zA-Z0-9_]{4,16}$")) {
            sender.sendMessage(text("This name is illegal", NamedTextColor.RED));
            return false;
        }

        if (Bukkit.getPlayerExact(name) != null || botList.getBotByName(name) != null) {
            sender.sendMessage(text("This bot is already in server", NamedTextColor.RED));
            return false;
        }

        if (LeavesConfig.modify.fakeplayer.unableNames.contains(name)) {
            sender.sendMessage(text("This name is not allowed", NamedTextColor.RED));
            return false;
        }

        if (botList.bots.size() >= LeavesConfig.modify.fakeplayer.limit) {
            sender.sendMessage(text("Bot number limit exceeded", NamedTextColor.RED));
            return false;
        }

        return true;
    }

    private static class NameArgument extends ArgumentNode<String> {

        public NameArgument() {
            super("name", StringArgumentType.word());
            children(SkinNameArgument::new);
        }

        @Override
        protected boolean execute(CommandContext context) throws CommandSyntaxException {
            return handleCreateCommand(context);
        }
    }

    private static class SkinNameArgument extends ArgumentNode<String> {

        public SkinNameArgument() {
            super("skin_name", StringArgumentType.word());
            children(WorldArgument::new);
        }

        @Override
        protected boolean execute(CommandContext context) throws CommandSyntaxException {
            return handleCreateCommand(context);
        }
    }

    private static class WorldArgument extends ArgumentNode<ResourceLocation> {

        public WorldArgument() {
            super("world", DimensionArgument.dimension());
            children(LocationArgument::new);
        }

        @Override
        public boolean requires(@NotNull CommandSourceStack source) {
            return source.getSender() instanceof ConsoleCommandSender;
        }
    }

    private static class LocationArgument extends ArgumentNode<Coordinates> {

        public LocationArgument() {
            super("location", Vec3Argument.vec3(true));
        }

        @Override
        protected boolean execute(CommandContext context) throws CommandSyntaxException {
            return handleCreateCommand(context);
        }
    }
}
