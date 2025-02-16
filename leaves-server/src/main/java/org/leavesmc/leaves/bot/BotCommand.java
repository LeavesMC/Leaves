package org.leavesmc.leaves.bot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.AbstractBotAction;
import org.leavesmc.leaves.bot.agent.AbstractBotConfig;
import org.leavesmc.leaves.bot.agent.Configs;
import org.leavesmc.leaves.bot.agent.actions.CraftCustomBotAction;
import org.leavesmc.leaves.command.CommandArgumentResult;
import org.leavesmc.leaves.command.LeavesCommandUtil;
import org.leavesmc.leaves.entity.Bot;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;
import org.leavesmc.leaves.event.bot.BotConfigModifyEvent;
import org.leavesmc.leaves.event.bot.BotCreateEvent;
import org.leavesmc.leaves.event.bot.BotRemoveEvent;
import org.leavesmc.leaves.plugin.MinecraftInternalPlugin;

import java.util.*;

import static net.kyori.adventure.text.Component.text;

public class BotCommand extends Command {

    private final Component unknownMessage;

    public BotCommand(String name) {
        super(name);
        this.description = "FakePlayer Command";
        this.usageMessage = "/bot [create | remove | action | list | config]";
        this.unknownMessage = text("Usage: " + usageMessage, NamedTextColor.RED);
        this.setPermission("bukkit.command.bot");
        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        if (pluginManager.getPermission("bukkit.command.bot") == null) {
            pluginManager.addPermission(new Permission("bukkit.command.bot", PermissionDefault.OP));
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String @NotNull [] args, Location location) throws IllegalArgumentException {
        List<String> list = new ArrayList<>();
        BotList botList = BotList.INSTANCE;

        if (args.length <= 1) {
            list.add("create");
            list.add("remove");
            if (LeavesConfig.modify.fakeplayer.canUseAction) {
                list.add("action");
            }
            if (LeavesConfig.modify.fakeplayer.canModifyConfig) {
                list.add("config");
            }
            if (LeavesConfig.modify.fakeplayer.canManualSaveAndLoad) {
                list.add("save");
                list.add("load");
            }
            list.add("list");
        }

        if (args.length == 2) {
            switch (args[0]) {
                case "create" -> list.add("<BotName>");
                case "remove", "action", "config", "save" -> list.addAll(botList.bots.stream().map(e -> e.getName().getString()).toList());
                case "list" -> list.addAll(Bukkit.getWorlds().stream().map(WorldInfo::getName).toList());
                case "load" -> list.addAll(botList.getSavedBotList().getAllKeys());
            }
        }

        if (args.length == 3) {
            switch (args[0]) {
                case "action" -> {
                    list.add("list");
                    list.add("stop");
                    list.addAll(Actions.getNames());
                }
                case "create" -> list.add("<BotSkinName>");
                case "config" -> list.addAll(acceptConfig);
                case "remove" -> list.addAll(List.of("cancel", "[hour]"));
            }
        }

        if (args[0].equals("remove") && args.length >= 3) {
            if (!Objects.equals(args[3], "cancel")) {
                switch (args.length) {
                    case 4 -> list.add("[minute]");
                    case 5 -> list.add("[second]");
                }
            }
        }

        if (args.length >= 4 && args[0].equals("action")) {
            ServerBot bot = botList.getBotByName(args[1]);

            if (bot == null) {
                return Collections.singletonList("<" + args[1] + " not found>");
            }

            if (args[2].equals("stop")) {
                list.add("all");
                for (int i = 0; i < bot.getBotActions().size(); i++) {
                    list.add(String.valueOf(i));
                }
            } else {
                AbstractBotAction<?> action = Actions.getForName(args[2]);
                if (action != null) {
                    list.addAll(action.getArgument().tabComplete(args.length - 4));
                }
            }
        }

        if (args.length >= 4 && args[0].equals("config")) {
            Configs<?> config = Configs.getConfig(args[2]);
            if (config != null) {
                list.addAll(config.config.getArgument().tabComplete(args.length - 4));
            }
        }

        return LeavesCommandUtil.getListMatchingLast(sender, args, list, "bukkit.command.bot.", "bukkit.command.bot");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        if (!testPermission(sender) || !LeavesConfig.modify.fakeplayer.enable) return true;

        if (args.length == 0) {
            sender.sendMessage(unknownMessage);
            return false;
        }

        switch (args[0]) {
            case "create" -> this.onCreate(sender, args);
            case "remove" -> this.onRemove(sender, args);
            case "action" -> this.onAction(sender, args);
            case "config" -> this.onConfig(sender, args);
            case "list" -> this.onList(sender, args);
            case "save" -> this.onSave(sender, args);
            case "load" -> this.onLoad(sender, args);
            default -> {
                sender.sendMessage(unknownMessage);
                return false;
            }
        }

        return true;
    }

    private void onCreate(CommandSender sender, String @NotNull [] args) {
        if (args.length < 2) {
            sender.sendMessage(text("Use /bot create <name> [skin_name] to create a fakeplayer", NamedTextColor.RED));
            return;
        }

        String botName = args[1];
        if (this.canCreate(sender, botName)) {
            BotCreateState.Builder builder = BotCreateState.builder(botName, Bukkit.getWorlds().getFirst().getSpawnLocation()).createReason(BotCreateEvent.CreateReason.COMMAND).creator(sender);

            if (args.length >= 3) {
                builder.skinName(args[2]);
            }

            if (sender instanceof Player player) {
                builder.location(player.getLocation());
            } else if (sender instanceof ConsoleCommandSender) {
                if (args.length >= 7) {
                    try {
                        World world = Bukkit.getWorld(args[3]);
                        double x = Double.parseDouble(args[4]);
                        double y = Double.parseDouble(args[5]);
                        double z = Double.parseDouble(args[6]);
                        if (world != null) {
                            builder.location(new Location(world, x, y, z));
                        }
                    } catch (Exception e) {
                        LeavesLogger.LOGGER.warning("Can't build location", e);
                    }
                }
            }

            builder.spawnWithSkin(null);
        }
    }

    private boolean canCreate(CommandSender sender, @NotNull String name) {
        BotList botList = BotList.INSTANCE;
        if (!name.matches("^[a-zA-Z0-9_]{4,16}$")) {
            sender.sendMessage(text("This name is illegal", NamedTextColor.RED));
            return false;
        }

        if (Bukkit.getPlayerExact(name) != null || botList.getBotByName(name) != null) {
            sender.sendMessage(text("This player is in server", NamedTextColor.RED));
            return false;
        }

        if (LeavesConfig.modify.fakeplayer.unableNames.contains(name)) {
            sender.sendMessage(text("This name is not allowed", NamedTextColor.RED));
            return false;
        }

        if (botList.bots.size() >= LeavesConfig.modify.fakeplayer.limit) {
            sender.sendMessage(text("Fakeplayer limit is full", NamedTextColor.RED));
            return false;
        }

        return true;
    }

    private void onRemove(CommandSender sender, String @NotNull [] args) {
        if (args.length < 2 || args.length > 5) {
            sender.sendMessage(text("Use /bot remove <name> [hour] [minute] [second] to remove a fakeplayer", NamedTextColor.RED));
            return;
        }

        BotList botList = BotList.INSTANCE;
        ServerBot bot = botList.getBotByName(args[1]);

        if (bot == null) {
            sender.sendMessage(text("This fakeplayer is not in server", NamedTextColor.RED));
            return;
        }

        if (args.length > 2) {
            if (args[2].equals("cancel")) {
                if (bot.removeTaskId == -1) {
                    sender.sendMessage(text("This fakeplayer is not scheduled to be removed", NamedTextColor.RED));
                    return;
                }
                Bukkit.getScheduler().cancelTask(bot.removeTaskId);
                bot.removeTaskId = -1;
                sender.sendMessage(text("Remove cancel"));
                return;
            }

            long time = 0;
            int h; // Preventing out-of-range
            long s = 0;
            long m = 0;

            try {
                h = Integer.parseInt(args[2]);
                if (h < 0) {
                    throw new NumberFormatException();
                }
                time += ((long) h) * 3600 * 20;
                if (args.length > 3) {
                    m = Long.parseLong(args[3]);
                    if (m > 59 || m < 0) {
                        throw new NumberFormatException();
                    }
                    time += m * 60 * 20;
                }
                if (args.length > 4) {
                    s = Long.parseLong(args[4]);
                    if (s > 59 || s < 0) {
                        throw new NumberFormatException();
                    }
                    time += s * 20;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(text("This fakeplayer is not scheduled to be removed", NamedTextColor.RED));
                return;
            }

            boolean isReschedule = bot.removeTaskId != -1;

            if (isReschedule) {
                Bukkit.getScheduler().cancelTask(bot.removeTaskId);
            }
            bot.removeTaskId = Bukkit.getScheduler().runTaskLater(MinecraftInternalPlugin.INSTANCE, () -> {
                bot.removeTaskId = -1;
                botList.removeBot(bot, BotRemoveEvent.RemoveReason.COMMAND, sender, false);
            }, time).getTaskId();

            sender.sendMessage("This fakeplayer will be removed in " + h + "h " + m + "m " + s + "s" + (isReschedule ? " (rescheduled)" : ""));

            return;
        }

        botList.removeBot(bot, BotRemoveEvent.RemoveReason.COMMAND, sender, false);
    }

    private void onAction(CommandSender sender, String @NotNull [] args) {
        if (!LeavesConfig.modify.fakeplayer.canUseAction) {
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(text("Use /bot action <name> <action> to make fakeplayer do action", NamedTextColor.RED));
            return;
        }

        ServerBot bot = BotList.INSTANCE.getBotByName(args[1]);
        if (bot == null) {
            sender.sendMessage(text("This fakeplayer is not in server", NamedTextColor.RED));
            return;
        }

        if (args[2].equals("list")) {
            sender.sendMessage(bot.getScoreboardName() + "'s action list:");
            for (int i = 0; i < bot.getBotActions().size(); i++) {
                sender.sendMessage(i + " " + bot.getBotActions().get(i).getName());
            }
            return;
        }

        if (args[2].equals("stop")) {
            if (args.length < 4) {
                sender.sendMessage(text("Invalid index", NamedTextColor.RED));
                return;
            }

            String index = args[3];
            if (index.equals("all")) {
                Set<AbstractBotAction<?>> forRemoval = new HashSet<>();
                for (int i = 0; i < bot.getBotActions().size(); i++) {
                    AbstractBotAction<?> action = bot.getBotActions().get(i);
                    BotActionStopEvent event = new BotActionStopEvent(
                            bot.getBukkitEntity(), action.getName(), action.getUUID(), BotActionStopEvent.Reason.COMMAND, sender
                    );
                    event.callEvent();
                    if (!event.isCancelled()) {
                        forRemoval.add(action);
                    }
                }
                bot.getBotActions().removeAll(forRemoval);
                sender.sendMessage(bot.getScoreboardName() + "'s action list cleared.");
            } else {
                try {
                    int i = Integer.parseInt(index);
                    if (i < 0 || i >= bot.getBotActions().size()) {
                        sender.sendMessage(text("Invalid index", NamedTextColor.RED));
                        return;
                    }

                    AbstractBotAction<?> action = bot.getBotActions().get(i);
                    BotActionStopEvent event = new BotActionStopEvent(
                            bot.getBukkitEntity(), action.getName(), action.getUUID(), BotActionStopEvent.Reason.COMMAND, sender
                    );
                    event.callEvent();
                    if (!event.isCancelled()) {
                        bot.getBotActions().remove(i);
                        sender.sendMessage(bot.getScoreboardName() + "'s " + action.getName() + " stopped.");

                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(text("Invalid index", NamedTextColor.RED));
                }
            }
            return;
        }

        AbstractBotAction<?> action = Actions.getForName(args[2]);
        if (action == null) {
            sender.sendMessage(text("Invalid action", NamedTextColor.RED));
            return;
        }

        CraftPlayer player;
        if (sender instanceof CraftPlayer) {
            player = (CraftPlayer) sender;
        } else {
            player = bot.getBukkitEntity();
        }

        String[] realArgs = new String[args.length - 3];
        if (realArgs.length != 0) {
            System.arraycopy(args, 3, realArgs, 0, realArgs.length);
        }

        AbstractBotAction<?> newAction;
        try {
            if (action instanceof CraftCustomBotAction customBotAction) {
                newAction = customBotAction.createCraft(player, realArgs);
            } else {
                newAction = action.create();
                newAction.loadCommand(player.getHandle(), action.getArgument().parse(0, realArgs));
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(text("Action create error, please check your arguments, " + e.getMessage(), NamedTextColor.RED));
            return;
        }

        if (newAction == null) {
            return;
        }

        if (bot.addBotAction(newAction, sender)) {
            sender.sendMessage("Action " + action.getName() + " has been issued to " + bot.getName().getString());
        }
    }

    private static final List<String> acceptConfig = Configs.getConfigs().stream().map(config -> config.config.getName()).toList();

    private void onConfig(CommandSender sender, String @NotNull [] args) {
        if (!LeavesConfig.modify.fakeplayer.canModifyConfig) {
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(text("Use /bot config <name> <config> to modify fakeplayer's config", NamedTextColor.RED));
            return;
        }

        ServerBot bot = BotList.INSTANCE.getBotByName(args[1]);
        if (bot == null) {
            sender.sendMessage(text("This fakeplayer is not in server", NamedTextColor.RED));
            return;
        }

        if (!acceptConfig.contains(args[2])) {
            sender.sendMessage(text("This config is not accept", NamedTextColor.RED));
            return;
        }

        AbstractBotConfig<?> config = Objects.requireNonNull(Configs.getConfig(args[2])).config;
        if (args.length < 4) {
            config.getMessage().forEach(sender::sendMessage);
        } else {
            String[] realArgs = new String[args.length - 3];
            System.arraycopy(args, 3, realArgs, 0, realArgs.length);

            BotConfigModifyEvent event = new BotConfigModifyEvent(bot.getBukkitEntity(), config.getName(), realArgs, sender);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }
            CommandArgumentResult result = config.getArgument().parse(0, realArgs);

            try {
                config.setValue(result);
                config.getChangeMessage().forEach(sender::sendMessage);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(text(e.getMessage(), NamedTextColor.RED));
            }
        }
    }

    private void onSave(CommandSender sender, String @NotNull [] args) {
        if (!LeavesConfig.modify.fakeplayer.canManualSaveAndLoad) {
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(text("Use /bot save <name> to save a fakeplayer", NamedTextColor.RED));
            return;
        }

        BotList botList = BotList.INSTANCE;
        ServerBot bot = botList.getBotByName(args[1]);

        if (bot == null) {
            sender.sendMessage(text("This fakeplayer is not in server", NamedTextColor.RED));
            return;
        }

        if (botList.removeBot(bot, BotRemoveEvent.RemoveReason.COMMAND, sender, true)) {
            sender.sendMessage(bot.getScoreboardName() + " saved to " + bot.createState.realName());
        }
    }

    private void onLoad(CommandSender sender, String @NotNull [] args) {
        if (!LeavesConfig.modify.fakeplayer.canManualSaveAndLoad) {
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(text("Use /bot save <name> to save a fakeplayer", NamedTextColor.RED));
            return;
        }

        String realName = args[1];
        BotList botList = BotList.INSTANCE;
        if (!botList.getSavedBotList().contains(realName)) {
            sender.sendMessage(text("This fakeplayer is not saved", NamedTextColor.RED));
            return;
        }

        if (botList.loadNewBot(realName) == null) {
            sender.sendMessage(text("Can't load bot, please check", NamedTextColor.RED));
        }
    }

    private void onList(CommandSender sender, String @NotNull [] args) {
        BotList botList = BotList.INSTANCE;
        if (args.length < 2) {
            Map<World, List<String>> botMap = new HashMap<>();
            for (World world : Bukkit.getWorlds()) {
                botMap.put(world, new ArrayList<>());
            }

            for (ServerBot bot : botList.bots) {
                Bot bukkitBot = bot.getBukkitEntity();
                botMap.get(bukkitBot.getWorld()).add(bukkitBot.getName());
            }

            sender.sendMessage("Total number: (" + botList.bots.size() + "/" + LeavesConfig.modify.fakeplayer.limit + ")");
            for (World world : botMap.keySet()) {
                sender.sendMessage(world.getName() + "(" + botMap.get(world).size() + "): " + formatPlayerNameList(botMap.get(world)));
            }
        } else {
            World world = Bukkit.getWorld(args[1]);

            if (world == null) {
                sender.sendMessage(text("Unknown world", NamedTextColor.RED));
                return;
            }

            List<String> snowBotList = new ArrayList<>();
            for (ServerBot bot : botList.bots) {
                Bot bukkitBot = bot.getBukkitEntity();
                if (bukkitBot.getWorld() == world) {
                    snowBotList.add(bukkitBot.getName());
                }
            }

            sender.sendMessage(world.getName() + "(" + botList.bots.size() + "): " + formatPlayerNameList(snowBotList));
        }
    }

    @NotNull
    private static String formatPlayerNameList(@NotNull List<String> list) {
        if (list.isEmpty()) {
            return "";
        }
        String string = list.toString();
        return string.substring(1, string.length() - 1);
    }
}
