package org.leavesmc.leaves.bot.subcommands;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.AbstractBotAction;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.actions.CraftCustomBotAction;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.command.LeavesSuggestionBuilder;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.kyori.adventure.text.Component.text;

public class BotActionCommand implements LeavesSubcommand {

    @Override
    public void execute(CommandSender sender, String subCommand, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(text("Use /bot action <name> <action> to make fakeplayer do action", NamedTextColor.RED));
            return;
        }

        ServerBot bot = BotList.INSTANCE.getBotByName(args[0]);
        if (bot == null) {
            sender.sendMessage(text("This fakeplayer is not in server", NamedTextColor.RED));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "list" -> {
                sender.sendMessage(bot.getScoreboardName() + "'s action list:");
                for (int i = 0; i < bot.getBotActions().size(); i++) {
                    sender.sendMessage(i + " " + bot.getBotActions().get(i).getName());
                }
            }
            case "start" -> executeStart(bot, sender, args);
            case "stop" -> executeStop(bot, sender, args);
        }
    }

    private void executeStart(ServerBot bot, CommandSender sender, String[] args) {
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

        String[] realArgs = Arrays.copyOfRange(args, 3, args.length);
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

    private void executeStop(ServerBot bot, CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(text("Invalid index", NamedTextColor.RED));
            return;
        }

        String index = args[2];
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
                    action.stop(bot, BotActionStopEvent.Reason.COMMAND);
                }
            }
            bot.getBotActions().removeAll(forRemoval);
            sender.sendMessage(bot.getScoreboardName() + "'s action list cleared.");
            return;
        }
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
                action.stop(bot, BotActionStopEvent.Reason.COMMAND);
                bot.getBotActions().remove(i);
                sender.sendMessage(bot.getScoreboardName() + "'s " + action.getName() + " stopped.");

            }
        } catch (NumberFormatException e) {
            sender.sendMessage(text("Invalid index", NamedTextColor.RED));
        }
    }

    @Override
    public void suggest(@NotNull CommandSender sender, @NotNull String subCommand, String @NotNull [] args, Location location, @NotNull LeavesSuggestionBuilder builder) {
        BotList botList = BotList.INSTANCE;
        ServerBot serverBot = null;

        if (args.length > 1 && (serverBot = botList.getBotByName(args[0])) == null) {
            builder.suggest("<" + args[0] + " not found>");
            return;
        }

        switch (args.length) {
            case 0, 1 -> botList.bots.forEach(bot -> builder.suggest(bot.getName().getString()));
            case 2 -> builder.suggest("start").suggest("stop").suggest("list");
            case 3 -> {
                switch (args[1].toLowerCase()) {
                    case "start" -> Actions.getNames().forEach(builder::suggest);
                    case "stop" -> {
                        builder.suggest("all");
                        int[] index = new int[]{0};
                        serverBot.getBotActions().forEach(a -> builder.suggest(String.valueOf(index[0]++)));
                    }
                }
            }
            case 4, 5, 6, 7 -> {
                AbstractBotAction<?> action = Actions.getForName(args[2]);
                if (action == null) {
                    return;
                }
                Pair<List<String>, String> results = action.getArgument().suggestion(args.length - 4, sender, args[args.length - 2]);
                if (results == null || results.getLeft() == null) {
                    return;
                }

                for (String s : results.getLeft()) {
                    if (results.getRight() != null) {
                        builder.suggest(s, Component.literal(results.getRight()));
                    } else {
                        builder.suggest(s);
                    }
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return LeavesConfig.modify.fakeplayer.canUseAction;
    }
}
