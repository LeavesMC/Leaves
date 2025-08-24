package org.leavesmc.leaves.command.bot.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.actions.AbstractBotAction;
import org.leavesmc.leaves.event.bot.BotActionStopEvent;
import org.leavesmc.leaves.neo_command.ArgumentNode;
import org.leavesmc.leaves.neo_command.CommandContext;
import org.leavesmc.leaves.neo_command.CustomArgumentNode;
import org.leavesmc.leaves.neo_command.LiteralNode;
import org.leavesmc.leaves.neo_command.WrappedArgument;
import org.leavesmc.leaves.neo_command.bot.BotSubcommand;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static io.papermc.paper.adventure.PaperAdventure.asAdventure;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.spaces;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static org.leavesmc.leaves.neo_command.bot.subcommands.ActionCommand.BotArgument.getBot;

public class ActionCommand extends BotSubcommand {

    public ActionCommand() {
        super("action");
        children(BotArgument::new);
    }

    @Override
    public boolean requires(@NotNull CommandSourceStack source) {
        return LeavesConfig.modify.fakeplayer.canUseAction && super.requires(source);
    }

    public static class BotArgument extends CustomArgumentNode<ServerBot, String> {

        protected BotArgument() {
            super("bot", new org.leavesmc.leaves.neo_command.bot.BotArgument());
            children(
                StartNode::new,
                StopNode::new,
                ListNode::new
            );
        }

        public static @NotNull ServerBot getBot(@NotNull CommandContext context) throws CommandSyntaxException {
            return context.getCustomArgument(BotArgument.class);
        }
    }

    private static class ListNode extends LiteralNode {

        protected ListNode() {
            super("list");
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) throws CommandSyntaxException {
            ServerBot bot = ActionCommand.BotArgument.getBot(context);

            CommandSender sender = context.getSender();
            List<AbstractBotAction<?>> actions = bot.getBotActions();
            if (actions.isEmpty()) {
                sender.sendMessage(text("This bot has no active actions", GRAY));
                return true;
            }

            sender.sendMessage(asAdventure(bot.getDisplayName()).append(text("'s action list:", GRAY)));
            for (int i = 0; i < actions.size(); i++) {
                AbstractBotAction<?> action = actions.get(i);
                sender.sendMessage(join(spaces(),
                    text(i, GRAY),
                    text(action.getName(), AQUA).hoverEvent(showText(text(action.getActionDataString())))
                ));
            }

            return true;
        }
    }

    private static class StartNode extends LiteralNode {

        protected StartNode() {
            super("start");
            Actions.getAll()
                .stream()
                .map(this::actionNodeCreator)
                .forEach(this::children);
        }

        private boolean handleStartCommand(CommandContext context, @NotNull AbstractBotAction<?> action) throws CommandSyntaxException {
            ServerBot bot = getBot(context);
            CommandSender sender = context.getSender();

            action.loadCommand(context);
            if (bot.addBotAction(action, sender)) {
                sender.sendMessage(join(spaces(),
                    text("Action", GRAY),
                    text(action.getName(), AQUA).hoverEvent(showText(text(action.getActionDataString()))),
                    text("has been issued to", GRAY),
                    asAdventure(bot.getDisplayName())
                ));
            }
            return true;
        }

        @Contract(pure = true)
        private @NotNull Supplier<LiteralNode> actionNodeCreator(AbstractBotAction<?> action) {
            return () -> new ActionLiteralNode(action);
        }

        private class ActionLiteralNode extends LiteralNode {
            private final AbstractBotAction<?> action;

            public ActionLiteralNode(@NotNull AbstractBotAction<?> action) {
                super(action.getName());
                this.action = action;
            }

            @Override
            protected ArgumentBuilder<CommandSourceStack, ?> compile() {
                ArgumentBuilder<CommandSourceStack, ?> builder = super.compile();

                Map<Integer, List<Pair<String, WrappedArgument<?>>>> arguments = action.getArguments();
                Command<CommandSourceStack> executor = context -> handleStartCommand(new CommandContext(context), action) ? Command.SINGLE_SUCCESS : 0;

                for (Map.Entry<Integer, List<Pair<String, WrappedArgument<?>>>> entry : arguments.entrySet()) {
                    List<Pair<String, WrappedArgument<?>>> value = entry.getValue();
                    ArgumentBuilder<CommandSourceStack, ?> branchArgumentBuilder = null;

                    for (Pair<String, WrappedArgument<?>> stringWrappedArgumentPair : value.reversed()) {
                        WrappedArgument<?> argument = stringWrappedArgumentPair.getRight();
                        if (branchArgumentBuilder == null) {
                            branchArgumentBuilder = argument.compile().executes(executor);
                        } else if (argument.isOptional()) {
                            branchArgumentBuilder = branchArgumentBuilder.executes(executor);
                        } else {
                            branchArgumentBuilder = argument.compile().then(branchArgumentBuilder);
                        }
                    }

                    if (value.getFirst().getRight().isOptional() || value.isEmpty()) {
                        builder = builder.executes(executor);
                    }

                    if (branchArgumentBuilder != null) {
                        builder = builder.then(branchArgumentBuilder);
                    }
                }

                if (arguments.isEmpty()) {
                    builder = builder.executes(executor);
                }

                return builder;
            }
        }
    }

    private static class StopNode extends LiteralNode {

        protected StopNode() {
            super("stop");
            children(StopIndexArgument::new);
            children(StopAll::new);
        }

        private static class StopIndexArgument extends ArgumentNode<Integer> {

            protected StopIndexArgument() {
                super("index", IntegerArgumentType.integer(0));
            }

            @Override
            protected CompletableFuture<Suggestions> getSuggestions(CommandContext context, SuggestionsBuilder builder) throws CommandSyntaxException {
                ServerBot bot = ActionCommand.BotArgument.getBot(context);

                for (int i = 0; i < bot.getBotActions().size(); i++) {
                    AbstractBotAction<?> action = bot.getBotActions().get(i);
                    builder.suggest(String.valueOf(i), Component.literal(action.getName()));
                }

                return builder.buildFuture();
            }

            @Override
            protected boolean execute(CommandContext context) throws CommandSyntaxException {
                ServerBot bot = ActionCommand.BotArgument.getBot(context);
                CommandSender sender = context.getSender();

                int index = context.getArgument(StopIndexArgument.class);
                int maxIndex = bot.getBotActions().size() - 1;
                if (maxIndex < 0) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
                }
                if (index > maxIndex) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().create(index, maxIndex);
                }

                AbstractBotAction<?> action = bot.getBotActions().get(index);
                BotActionStopEvent event = new BotActionStopEvent(
                    bot.getBukkitEntity(), action.getName(), action.getUUID(), BotActionStopEvent.Reason.COMMAND, sender
                );
                event.callEvent();
                if (!event.isCancelled()) {
                    action.stop(bot, BotActionStopEvent.Reason.COMMAND);
                    bot.getBotActions().remove(index);
                    sender.sendMessage(join(spaces(),
                        text("Already stopped", GRAY),
                        asAdventure(bot.getDisplayName()).append(text("'s", GRAY)),
                        text("action", GRAY),
                        text(action.getName(), AQUA).hoverEvent(showText(text(action.getActionDataString())))
                    ));
                } else {
                    sender.sendMessage(text("Action stop cancelled by a plugin", RED));
                }
                return true;
            }
        }

        private static class StopAll extends LiteralNode {

            public StopAll() {
                super("all");
            }

            @Override
            protected boolean execute(@NotNull CommandContext context) throws CommandSyntaxException {
                ServerBot bot = ActionCommand.BotArgument.getBot(context);

                List<AbstractBotAction<?>> actions = bot.getBotActions();
                CommandSender sender = context.getSender();
                if (actions.isEmpty()) {
                    sender.sendMessage(text("This bot has no active actions", GRAY));
                    return true;
                }

                Set<AbstractBotAction<?>> canceled = new HashSet<>();
                Set<AbstractBotAction<?>> forRemoval = new HashSet<>();
                for (AbstractBotAction<?> action : actions) {
                    BotActionStopEvent event = new BotActionStopEvent(
                        bot.getBukkitEntity(), action.getName(), action.getUUID(), BotActionStopEvent.Reason.COMMAND, sender
                    );
                    event.callEvent();
                    if (!event.isCancelled()) {
                        forRemoval.add(action);
                        action.stop(bot, BotActionStopEvent.Reason.COMMAND);
                    } else {
                        canceled.add(action);
                    }
                }
                bot.getBotActions().removeAll(forRemoval);

                if (canceled.isEmpty()) {
                    sender.sendMessage(join(spaces(),
                        asAdventure(bot.getDisplayName()).append(text("'s", GRAY)),
                        text("'s action list cleared", GRAY)
                    ));
                    return true;
                }

                sender.sendMessage(join(spaces(),
                    text("Tried to clear", GRAY),
                    asAdventure(bot.getDisplayName()).append(text("'s", GRAY)),
                    text("'s action list, but following actions' stop was canceled by plugin:", GRAY)
                ));
                for (AbstractBotAction<?> action : canceled) {
                    context.getSender().sendMessage(
                        text(action.getName(), AQUA).hoverEvent(showText(text(action.getActionDataString())))
                    );
                }
                return true;
            }
        }
    }
}