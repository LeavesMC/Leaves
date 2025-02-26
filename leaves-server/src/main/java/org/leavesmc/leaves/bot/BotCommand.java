package org.leavesmc.leaves.bot;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.subcommands.BotActionCommand;
import org.leavesmc.leaves.bot.subcommands.BotConfigCommand;
import org.leavesmc.leaves.bot.subcommands.BotCreateCommand;
import org.leavesmc.leaves.bot.subcommands.BotListCommand;
import org.leavesmc.leaves.bot.subcommands.BotLoadCommand;
import org.leavesmc.leaves.bot.subcommands.BotRemoveCommand;
import org.leavesmc.leaves.bot.subcommands.BotSaveCommand;
import org.leavesmc.leaves.command.LeavesCommandUtil;
import org.leavesmc.leaves.command.LeavesSubcommand;

import java.util.*;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public class BotCommand extends Command {

    public BotCommand(String name) {
        super(name);
        this.description = "FakePlayer Command";
        this.usageMessage = "/bot [" + String.join(" | ", usableSubcommands()) + "]";
        this.setPermission("bukkit.command.bot");
        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        if (pluginManager.getPermission("bukkit.command.bot") == null) {
            pluginManager.addPermission(new Permission("bukkit.command.bot", PermissionDefault.OP));
        }
    }

    // subcommand label -> subcommand
    private static final Map<String, LeavesSubcommand> SUBCOMMANDS = Util.make(() -> {
        final Map<Set<String>, LeavesSubcommand> commands = new HashMap<>();
        commands.put(Set.of("create"), new BotCreateCommand());
        commands.put(Set.of("remove"), new BotRemoveCommand());
        commands.put(Set.of("action"), new BotActionCommand());
        commands.put(Set.of("config"), new BotConfigCommand());
        commands.put(Set.of("save"), new BotSaveCommand());
        commands.put(Set.of("load"), new BotLoadCommand());
        commands.put(Set.of("list"), new BotListCommand());

        return commands.entrySet().stream()
                .flatMap(entry -> entry.getKey().stream().map(s -> Map.entry(s, entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    });

    @NotNull
    @Override
    public List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String alias, final String[] args, final @Nullable Location location) throws IllegalArgumentException {
        if (args.length <= 1) {
            return LeavesCommandUtil.getListMatchingLast(sender, args, usableSubcommands(), "bukkit.command.bot.", "bukkit.command.bot");
        }

        final @Nullable Pair<String, LeavesSubcommand> subCommand = resolveCommand(args[0]);
        if (subCommand != null) {
            var list = subCommand.second().tabComplete(sender, subCommand.first(), Arrays.copyOfRange(args, 1, args.length), location);
            return LeavesCommandUtil.getListMatchingLast(sender, args, list, "bukkit.command.bot.", "bukkit.command.bot");
        }

        return Collections.emptyList();
    }

    @Override
    public boolean execute(final @NotNull CommandSender sender, final @NotNull String commandLabel, final String @NotNull [] args) {
        if (!testPermission(sender) || !LeavesConfig.modify.fakeplayer.enable) return true;

        if (args.length == 0) {
            sender.sendMessage(unknownMessage());
            return false;
        }
        final Pair<String, LeavesSubcommand> subCommand = resolveCommand(args[0]);

        if (subCommand == null) {
            sender.sendMessage(unknownMessage());
            return false;
        }

        final String[] choppedArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.second().execute(sender, subCommand.first(), choppedArgs);
    }

    @Nullable
    private static Pair<String, LeavesSubcommand> resolveCommand(String label) {
        label = label.toLowerCase(Locale.ENGLISH);
        LeavesSubcommand subCommand = SUBCOMMANDS.get(label);

        if (subCommand != null) {
            return Pair.of(label, subCommand);
        }

        return null;
    }

    public Collection<String> usableSubcommands() {
        return SUBCOMMANDS.entrySet().stream().filter(entry -> entry.getValue().tabCompletes()).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public Component unknownMessage() {
        return text("Usage: /bot [" + String.join(" | ", usableSubcommands()) + "]", NamedTextColor.RED);
    }
}