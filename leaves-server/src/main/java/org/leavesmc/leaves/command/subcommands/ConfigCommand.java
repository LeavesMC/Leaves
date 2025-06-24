package org.leavesmc.leaves.command.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.command.LeavesCommandUtil;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.command.LeavesSuggestionBuilder;
import org.leavesmc.leaves.config.GlobalConfigManager;
import org.leavesmc.leaves.config.VerifiedConfig;

public class ConfigCommand implements LeavesSubcommand {

    @Override
    public void execute(CommandSender sender, String subCommand, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Leaves Config", NamedTextColor.GRAY));
            return;
        }

        VerifiedConfig verifiedConfig = GlobalConfigManager.getVerifiedConfig(args[0]);
        if (verifiedConfig == null) {
            sender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                Component.text("Config ", NamedTextColor.GRAY),
                Component.text(args[0], NamedTextColor.RED),
                Component.text(" is Not Found.", NamedTextColor.GRAY)
            ));
            return;
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
    }

    @Override
    public void suggest(@NotNull CommandSender sender, @NotNull String subCommand, String @NotNull [] args, @Nullable Location location, @NotNull LeavesSuggestionBuilder builder) {
        if (args.length <= 1) {
            String arg = args[0];
            int dotIndex = arg.lastIndexOf(".");
            builder.createOffset(builder.getInput().lastIndexOf(' ') + dotIndex + 2);
            LeavesCommandUtil.getListClosestMatchingLast(sender, arg.substring(dotIndex + 1), GlobalConfigManager.getVerifiedConfigSubPaths(arg), "bukkit.command.leaves.config")
                .forEach(builder::suggest);
        }
        if (args.length == 2) {
            VerifiedConfig verifiedConfig = GlobalConfigManager.getVerifiedConfig(args[0]);
            if (verifiedConfig != null) {
                LeavesCommandUtil.getListMatchingLast(sender, args, verifiedConfig.validator().valueSuggest()).forEach(builder::suggest);
            } else {
                builder.suggest("<ERROR CONFIG>");
            }
        }
    }
}
