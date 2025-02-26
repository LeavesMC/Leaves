package org.leavesmc.leaves.command.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.leavesmc.leaves.command.LeavesCommandUtil;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.config.GlobalConfigManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigCommand implements LeavesSubcommand {

    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Leaves Config", NamedTextColor.GRAY));
            return true;
        }

        GlobalConfigManager.VerifiedConfig verifiedConfig = GlobalConfigManager.getVerifiedConfig(args[0]);
        if (verifiedConfig == null) {
            sender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                Component.text("Config ", NamedTextColor.GRAY),
                Component.text(args[0], NamedTextColor.RED),
                Component.text(" is Not Found.", NamedTextColor.GRAY)
            ));
            return true;
        }

        if (args.length > 1) {
            try {
                verifiedConfig.set(args[1]);
                sender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                    Component.text("Config ", NamedTextColor.GRAY),
                    Component.text(args[0], NamedTextColor.AQUA),
                    Component.text(" changed to ", NamedTextColor.GRAY),
                    Component.text(verifiedConfig.getString(), NamedTextColor.AQUA)
                ));
            } catch (IllegalArgumentException exception) {
                sender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                    Component.text("Config ", NamedTextColor.GRAY),
                    Component.text(args[0], NamedTextColor.RED),
                    Component.text(" modify error by ", NamedTextColor.GRAY),
                    Component.text(exception.getMessage(), NamedTextColor.RED)
                ));
            }
        } else {
            sender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                Component.text("Config ", NamedTextColor.GRAY),
                Component.text(args[0], NamedTextColor.AQUA),
                Component.text(" value is ", NamedTextColor.GRAY),
                Component.text(verifiedConfig.getString(), NamedTextColor.AQUA)
            ));
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String[] args, Location location) {
        switch (args.length) {
            case 1 -> {
                List<String> list = new ArrayList<>(GlobalConfigManager.getVerifiedConfigPaths());
                return LeavesCommandUtil.getListMatchingLast(sender, args, list);
            }

            case 2 -> {
                GlobalConfigManager.VerifiedConfig verifiedConfig = GlobalConfigManager.getVerifiedConfig(args[0]);
                if (verifiedConfig != null) {
                    return LeavesCommandUtil.getListMatchingLast(sender, args, verifiedConfig.validator().valueSuggest());
                } else {
                    return Collections.singletonList("<ERROR CONFIG>");
                }
            }
        }

        return Collections.emptyList();
    }
}
