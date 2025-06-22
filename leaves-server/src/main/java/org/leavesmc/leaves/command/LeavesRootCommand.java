package org.leavesmc.leaves.command;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public abstract class LeavesRootCommand extends Command implements LeavesSuggestionCommand {

    protected final String basePermission;
    protected final Map<String, LeavesSubcommand> subcommands;

    protected LeavesRootCommand(
        @NotNull String name,
        @NotNull String description,
        @NotNull String basePermission,
        @NotNull Map<String, LeavesSubcommand> subCommands
    ) {
        super(name, description, String.format("/%s [%s]", name, String.join(" | ", subCommands.keySet())), Collections.emptyList());
        this.basePermission = basePermission;
        this.subcommands = subCommands;

        final List<String> permissions = new ArrayList<>();
        permissions.add(basePermission);
        permissions.addAll(subCommands.keySet().stream().map(s -> basePermission + "." + s).toList());
        this.setPermission(String.join(";", permissions));
        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        for (final String perm : permissions) {
            if (pluginManager.getPermission(perm) == null) {
                pluginManager.addPermission(new Permission(perm, PermissionDefault.OP));
            }
        }
    }

    protected boolean testPermission(final CommandSender sender, final String permission) {
        if (sender.hasPermission(basePermission) || sender.hasPermission(basePermission + "." + permission)) {
            return true;
        }
        sender.sendMessage(Bukkit.permissionMessage());
        return false;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!testPermission(sender) || !isEnabled()) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(unknownMessage());
            return true;
        }
        final Pair<String, LeavesSubcommand> subCommand = resolveCommand(args[0]);

        if (subCommand == null) {
            sender.sendMessage(unknownMessage());
            return true;
        }

        if (!testPermission(sender, subCommand.first())) {
            return true;
        }
        final String[] choppedArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.second().execute(sender, subCommand.first(), choppedArgs);
        return true;
    }

    @Override
    public void suggest(final @NotNull CommandSender sender, final @NotNull String alias, final @NotNull String @NotNull [] args, final @Nullable Location location, @NotNull LeavesSuggestionBuilder builder) throws IllegalArgumentException {
        if (!testPermission(sender) || !isEnabled()) {
            return;
        }
        if (args.length <= 1) {
            LeavesCommandUtil.getListMatchingLast(sender, args, usableSubcommands(), basePermission + ".", basePermission).forEach(builder::suggest);
            return;
        }
        final @Nullable Pair<String, LeavesSubcommand> subCommand = resolveCommand(args[0]);
        if (subCommand != null) {
            subCommand.second().suggest(sender, subCommand.first(), Arrays.copyOfRange(args, 1, args.length), location, builder);
        }
    }

    public Component unknownMessage() {
        return text(String.format("Usage: /%s [%s]", this.getName(), String.join(" | ", usableSubcommands())), RED);
    }

    @Nullable
    public Pair<String, LeavesSubcommand> resolveCommand(String label) {
        label = label.toLowerCase(Locale.ENGLISH);
        LeavesSubcommand subCommand = subcommands.get(label);

        if (subCommand != null && subCommand.isEnabled()) {
            return Pair.of(label, subCommand);
        }

        return null;
    }

    public Collection<String> usableSubcommands() {
        List<String> subcommandList = new ArrayList<>();
        for (var entry : subcommands.entrySet()) {
            if (entry.getValue().isEnabled()) {
                subcommandList.add(entry.getKey());
            }
        }
        return subcommandList;
    }
}
