package org.leavesmc.leaves.neo_command.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.command.LeavesCommandUtil;
import org.leavesmc.leaves.config.GlobalConfigManager;
import org.leavesmc.leaves.config.VerifiedConfig;
import org.leavesmc.leaves.neo_command.ArgumentNode;
import org.leavesmc.leaves.neo_command.CommandContext;
import org.leavesmc.leaves.neo_command.LiteralNode;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;

public class ConfigCommand extends LiteralNode {

    public ConfigCommand() {
        super("config");
        children(PathArgument::new);
    }

    private static class PathArgument extends ArgumentNode<String> {

        public PathArgument() {
            super("path", StringArgumentType.string());
            children(ValueArgument::new);
        }

        @Override
        protected CompletableFuture<Suggestions> getSuggestions(@NotNull CommandContext context, @NotNull SuggestionsBuilder builder) {
            String path = context.getArgumentOrDefault(PathArgument.class, "");
            int dotIndex = path.lastIndexOf(".");
            builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + dotIndex + 2);
            LeavesCommandUtil.getListClosestMatchingLast(
                    context.getSender(),
                    path.substring(dotIndex + 1),
                    GlobalConfigManager.getVerifiedConfigSubPaths(path),
                    "bukkit.command.leaves.config"
                )
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
            context.getSender().sendMessage(Component.join(JoinConfiguration.spaces(),
                text("Config", NamedTextColor.GRAY),
                text(path, NamedTextColor.AQUA),
                text("value is", NamedTextColor.GRAY),
                text(verifiedConfig.getString(), NamedTextColor.AQUA)
            ));
            return true;
        }

        private static @Nullable VerifiedConfig getVerifiedConfig(@NotNull CommandContext context) {
            String path = context.getArgument(PathArgument.class);
            VerifiedConfig verifiedConfig = GlobalConfigManager.getVerifiedConfig(path);
            if (verifiedConfig == null) {
                context.getSender().sendMessage(Component.join(JoinConfiguration.spaces(),
                    text("Config", NamedTextColor.GRAY),
                    text(path, NamedTextColor.RED),
                    text("is Not Found.", NamedTextColor.GRAY)
                ));
                return null;
            }
            return verifiedConfig;
        }

        private static class ValueArgument extends ArgumentNode<String> {

            public ValueArgument() {
                super("value", StringArgumentType.greedyString());
            }

            @Override
            protected CompletableFuture<Suggestions> getSuggestions(@NotNull CommandContext context, @NotNull SuggestionsBuilder builder) {
                String path = context.getArgument(PathArgument.class);
                String value = context.getArgumentOrDefault(ValueArgument.class, "");
                VerifiedConfig verifiedConfig = GlobalConfigManager.getVerifiedConfig(path);
                if (verifiedConfig == null) {
                    return builder
                        .suggest("<ERROR CONFIG>", net.minecraft.network.chat.Component.literal("This config path does not exist."))
                        .buildFuture();
                }
                LeavesCommandUtil.getListMatchingLast(
                    context.getSender(),
                    new String[]{value},
                    verifiedConfig.validator().valueSuggest()
                ).forEach(builder::suggest);
                return builder.buildFuture();
            }

            @Override
            protected boolean execute(@NotNull CommandContext context) {
                VerifiedConfig verifiedConfig = getVerifiedConfig(context);
                String path = context.getArgument(PathArgument.class);
                String value = context.getArgument(ValueArgument.class);
                if (verifiedConfig == null) {
                    return false;
                }
                try {
                    verifiedConfig.set(value);
                    context.getSender().sendMessage(Component.join(JoinConfiguration.spaces(),
                        text("Config", NamedTextColor.GRAY),
                        text(path, NamedTextColor.AQUA),
                        text("changed to", NamedTextColor.GRAY),
                        text(verifiedConfig.getString(), NamedTextColor.AQUA)
                    ));
                    Bukkit.getOnlinePlayers()
                        .stream()
                        .filter(player -> player.hasPermission("leaves.command.config.notify") && player != context.getSender())
                        .forEach(
                            player -> player.sendMessage(Component.join(JoinConfiguration.spaces(),
                                text(context.getSender().getName() + ":", NamedTextColor.GRAY),
                                text("Config", NamedTextColor.GRAY),
                                text(path, NamedTextColor.AQUA),
                                text("changed to", NamedTextColor.GRAY),
                                text(verifiedConfig.getString(), NamedTextColor.AQUA)
                            ))
                        );
                    return true;
                } catch (IllegalArgumentException exception) {
                    context.getSender().sendMessage(Component.join(JoinConfiguration.spaces(),
                        text("Config", NamedTextColor.GRAY),
                        text(path, NamedTextColor.RED),
                        text("modify error by", NamedTextColor.GRAY),
                        text(exception.getMessage(), NamedTextColor.RED)
                    ));
                    return false;
                }
            }
        }
    }
}