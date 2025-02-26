package org.leavesmc.leaves.bot.subcommands;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.AbstractBotAction;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.actions.CraftCustomBotAction;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.kyori.adventure.text.Component.text;

public class BotActionCommand implements LeavesSubcommand {
    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        if (!LeavesConfig.modify.fakeplayer.canUseAction) {
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(text("Use /bot action <name> <action> to make fakeplayer do action", NamedTextColor.RED));
            return false;
        }

        ServerBot bot = BotList.INSTANCE.getBotByName(args[0]);
        if (bot == null) {
            sender.sendMessage(text("This fakeplayer is not in server", NamedTextColor.RED));
            return false;
        }

        if (args[1].equals("list")) {
            sender.sendMessage(bot.getScoreboardName() + "'s action list:");
            for (int i = 0; i < bot.getBotActions().size(); i++) {
                sender.sendMessage(i + " " + bot.getBotActions().get(i).getName());
            }
            return false;
        }

        if (args[1].equals("stop")) {
            if (args.length < 3) {
                sender.sendMessage(text("Invalid index", NamedTextColor.RED));
                return false;
            }

            String index = args[4];
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
                        return false;
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
            return false;
        }

        AbstractBotAction<?> action = Actions.getForName(args[1]);
        if (action == null) {
            sender.sendMessage(text("Invalid action", NamedTextColor.RED));
            return false;
        }

        CraftPlayer player;
        if (sender instanceof CraftPlayer) {
            player = (CraftPlayer) sender;
        } else {
            player = bot.getBukkitEntity();
        }

        String[] realArgs = new String[args.length - 2];
        if (realArgs.length != 0) {
            System.arraycopy(args, 2, realArgs, 0, realArgs.length);
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
            return false;
        }

        if (newAction == null) {
            return false;
        }

        if (bot.addBotAction(newAction, sender)) {
            sender.sendMessage("Action " + action.getName() + " has been issued to " + bot.getName().getString());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String[] args, Location location) {
        if (!LeavesConfig.modify.fakeplayer.canUseAction) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        BotList botList = BotList.INSTANCE;

        if (args.length <= 1) {
            list.addAll(botList.bots.stream().map(e -> e.getName().getString()).toList());
        }

        if (args.length == 2) {
            list.add("list");
            list.add("stop");
            list.addAll(Actions.getNames());
        }

        if (args.length >= 3) {
            ServerBot bot = botList.getBotByName(args[0]);

            if (bot == null) {
                return Collections.singletonList("<" + args[0] + " not found>");
            }

            if (args[1].equals("stop")) {
                list.add("all");
                for (int i = 0; i < bot.getBotActions().size(); i++) {
                    list.add(String.valueOf(i));
                }
            } else {
                AbstractBotAction<?> action = Actions.getForName(args[1]);
                if (action != null) {
                    list.addAll(action.getArgument().tabComplete(args.length - 3));
                }
            }
        }

        return list;
    }

    @Override
    public boolean tabCompletes() {
        return LeavesConfig.modify.fakeplayer.canUseAction;
    }
}
