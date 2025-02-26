package org.leavesmc.leaves.command.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.leavesmc.leaves.command.LeavesCommandUtil;
import org.leavesmc.leaves.command.LeavesSubcommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PeacefulModeSwitchCommand implements LeavesSubcommand {

    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        World world;
        if (args.length == 0) {
            if (sender instanceof Player player) {
                world = player.getWorld();
            } else {
                sender.sendMessage(Component.text("Must specify a world! ex: '/leaves peaceful world'", NamedTextColor.RED));
                return true;
            }
        } else {
            final String input = args[0];
            final World inputWorld = Bukkit.getWorld(input);
            if (inputWorld == null) {
                sender.sendMessage(Component.text("'" + input + "' is not a valid world!", NamedTextColor.RED));
                return true;
            } else {
                world = inputWorld;
            }
        }

        final ServerLevel level = ((CraftWorld) world).getHandle();
        int chunks = 0;
        if (level.chunkSource.getLastSpawnState() != null) {
            chunks = level.chunkSource.getLastSpawnState().getSpawnableChunkCount();
        }

        sender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("Peaceful Mode Switch for world: "),
            Component.text(world.getName(), NamedTextColor.AQUA)
        ));

        sender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("Refuses per "),
            Component.text(level.chunkSource.peacefulModeSwitchTick, level.chunkSource.peacefulModeSwitchTick > 0 ? NamedTextColor.AQUA : NamedTextColor.GRAY),
            Component.text(" tick")
        ));

        int count = level.chunkSource.peacefulModeSwitchCount;
        int limit = NaturalSpawner.globalLimitForCategory(level, MobCategory.MONSTER, chunks);
        NamedTextColor color = count >= limit ? NamedTextColor.AQUA : NamedTextColor.GRAY;

        sender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("Now count "),
            Component.text(count, color),
            Component.text("/", color),
            Component.text(limit, color)
        ));

        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String subCommand, final String[] args, Location location) {
        return LeavesCommandUtil.getListMatchingLast(sender, args, this.suggestPeacefulModeSwitch(args));
    }

    private List<String> suggestPeacefulModeSwitch(final String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(Bukkit.getWorlds().stream().map(World::getName).toList());
        }

        return Collections.emptyList();
    }
}
