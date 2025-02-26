package org.leavesmc.leaves.command.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.command.LeavesCommandUtil;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.util.HopperCounter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CounterCommand implements LeavesSubcommand {

    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        if (!LeavesConfig.modify.hopperCounter) {
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                Component.text("Hopper Counter: ", NamedTextColor.GRAY),
                Component.text(HopperCounter.isEnabled(), HopperCounter.isEnabled() ? NamedTextColor.AQUA : NamedTextColor.GRAY)
            ));
            return true;
        }

        if (!HopperCounter.isEnabled()) {
            if (args[0].equals("enable")) {
                HopperCounter.setEnabled(true);
                sender.sendMessage(Component.text("Hopper Counter now is enabled", NamedTextColor.AQUA));
            } else {
                sender.sendMessage(Component.text("Hopper Counter is not enabled", NamedTextColor.RED));
            }
            return true;
        }

        DyeColor color = DyeColor.byName(args[0], null);
        if (color != null) {
            HopperCounter counter = HopperCounter.getCounter(color);
            if (args.length < 2) {
                displayCounter(sender, counter, false);
            } else {
                switch (args[1]) {
                    case "reset" -> {
                        counter.reset(MinecraftServer.getServer());
                        sender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                            Component.text("Restarted "),
                            Component.text(color.getName(), TextColor.color(color.getTextColor())),
                            Component.text(" counter")
                        ));
                    }
                    case "realtime" -> displayCounter(sender, counter, true);
                }
            }
            return true;
        }

        switch (args[0]) {
            case "reset" -> {
                HopperCounter.resetAll(MinecraftServer.getServer(), false);
                sender.sendMessage(Component.text("Restarted all counters"));
            }
            case "disable" -> {
                HopperCounter.setEnabled(false);
                HopperCounter.resetAll(MinecraftServer.getServer(), true);
                sender.sendMessage(Component.text("Hopper Counter now is disabled", NamedTextColor.GRAY));
            }
        }

        return true;
    }

    private void displayCounter(CommandSender sender, @NotNull HopperCounter counter, boolean realTime) {
        for (Component component : counter.format(MinecraftServer.getServer(), realTime)) {
            sender.sendMessage(component);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String[] args, Location location) {
        if (!LeavesConfig.modify.hopperCounter) {
            return Collections.emptyList();
        }

        switch (args.length) {
            case 1 -> {
                if (!HopperCounter.isEnabled()) {
                    return Collections.singletonList("enable");
                }

                List<String> list = new ArrayList<>(Arrays.stream(DyeColor.values()).map(DyeColor::getName).toList());
                list.add("reset");
                list.add("disable");
                return LeavesCommandUtil.getListMatchingLast(sender, args, list);
            }

            case 2 -> {
                if (DyeColor.byName(args[0], null) != null) {
                    return LeavesCommandUtil.getListMatchingLast(sender, args, "reset", "realtime");
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean tabCompletes() {
        return LeavesConfig.modify.hopperCounter;
    }
}
