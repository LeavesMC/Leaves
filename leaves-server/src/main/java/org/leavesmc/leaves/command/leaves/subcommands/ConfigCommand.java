package org.leavesmc.leaves.command.leaves.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.command.ArgumentNode;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.CommandUtils;
import org.leavesmc.leaves.command.leaves.LeavesSubcommand;
import org.leavesmc.leaves.config.GlobalConfigManager;
import org.leavesmc.leaves.config.VerifiedConfig;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.spaces;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ConfigCommand extends LeavesSubcommand {

    public ConfigCommand() {
        super("config");
        children(PathArgument::new);
    }

    private static class PathArgument extends ArgumentNode<String> {

        private PathArgument() {
            super("path", StringArgumentType.string());
            children(ValueArgument::new);
        }

        @Override
        protected CompletableFuture<Suggestions> getSuggestions(@NotNull CommandContext context, @NotNull SuggestionsBuilder builder) {
            String path = context.getArgumentOrDefault(PathArgument.class, "");
            int dotIndex = path.lastIndexOf(".");
            builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + dotIndex + 2);
            CommandUtils.getListClosestMatchingLast(path.substring(dotIndex + 1), GlobalConfigManager.getVerifiedConfigSubPaths(path))
                .forEach(builder::suggest);
            return builder.buildFuture();
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) {
            String path = context.getArgument(PathArgument.class);
            VerifiedConfig verifiedConfig = getVerifiedConfig(context);
            if (verifiedConfig == null) {
                return false;
            }
            context.getSender().sendMessage(join(spaces(),
                text("Config", GRAY),
                text(path, AQUA),
                text("value is", GRAY),
                text(verifiedConfig.getString(), AQUA)
            ));
            return true;
        }

        private static @Nullable VerifiedConfig getVerifiedConfig(@NotNull CommandContext context) {
            String path = context.getArgument(PathArgument.class);
            VerifiedConfig verifiedConfig = GlobalConfigManager.getVerifiedConfig(path);
            if (verifiedConfig == null) {
                context.getSender().sendMessage(join(spaces(),
                    text("Config", GRAY),
                    text(path, RED),
                    text("is Not Found.", GRAY)
                ));
                return null;
            }
            return verifiedConfig;
        }
    }

    private static class ValueArgument extends ArgumentNode<String> {

        private ValueArgument() {
            super("value", StringArgumentType.greedyString());
        }

        @Override
        protected CompletableFuture<Suggestions> getSuggestions(@NotNull CommandContext context, @NotNull SuggestionsBuilder builder) {
            String path = context.getArgument(PathArgument.class);
            VerifiedConfig verifiedConfig = GlobalConfigManager.getVerifiedConfig(path);
            if (verifiedConfig == null) {
                return builder
                    .suggest("<ERROR CONFIG>", net.minecraft.network.chat.Component.literal("This config path does not exist."))
                    .buildFuture();
            }
            verifiedConfig.validator().valueSuggest().forEach(builder::suggest);
            return builder.buildFuture();
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) {
            VerifiedConfig verifiedConfig = PathArgument.getVerifiedConfig(context);
            String path = context.getArgument(PathArgument.class);
            String value = context.getArgument(ValueArgument.class);
            if (verifiedConfig == null) {
                return false;
            }
            try {
                verifiedConfig.set(value);
                context.getSender().sendMessage(join(spaces(),
                    text("Config", GRAY),
                    text(path, AQUA),
                    text("changed to", GRAY),
                    text(verifiedConfig.getString(), AQUA)
                ));
                Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player -> player.hasPermission("leaves.command.config.notify") && player != context.getSender())
                    .forEach(
                        player -> player.sendMessage(join(spaces(),
                            text(context.getSender().getName() + ":", GRAY),
                            text("Config", GRAY),
                            text(path, AQUA),
                            text("changed to", GRAY),
                            text(verifiedConfig.getString(), AQUA)
                        ))
                    );
                return true;
            } catch (IllegalArgumentException exception) {
                context.getSender().sendMessage(join(spaces(),
                    text("Config", GRAY),
                    text(path, RED),
                    text("modify error by", GRAY),
                    text(exception.getMessage(), RED)
                ));
                return false;
            }
        }
    }
}