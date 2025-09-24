package org.leavesmc.leaves.command.bot.subcommands.action;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.actions.AbstractBotAction;
import org.leavesmc.leaves.bot.agent.actions.custom.ServerCustomAction;
import org.leavesmc.leaves.command.ArgumentNode;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.LiteralNode;
import org.leavesmc.leaves.command.WrappedArgument;
import org.leavesmc.leaves.entity.bot.action.custom.CustomAction;
import org.leavesmc.leaves.entity.bot.action.custom.CustomActionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static io.papermc.paper.adventure.PaperAdventure.asAdventure;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.spaces;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static org.leavesmc.leaves.command.bot.subcommands.ActionCommand.BotArgument.getBot;

public class StartCommand extends LiteralNode {

    public StartCommand() {
        super("start");
        Actions.getAll().stream().map(this::actionNodeCreator).forEach(this::children);
        this.children.add(new CustomActionNode());
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

        private ActionLiteralNode(@NotNull AbstractBotAction<?> action) {
            super(action.getName());
            this.action = action;
        }

        @Override
        protected ArgumentBuilder<CommandSourceStack, ?> compile() {
            ArgumentBuilder<CommandSourceStack, ?> builder = super.compile();

            Map<Integer, List<Pair<String, WrappedArgument<?>>>> arguments = action.getArguments();
            Command<CommandSourceStack> executor = context -> {
                if (handleStartCommand(new CommandContext(context), action)) {
                    return Command.SINGLE_SUCCESS;
                } else {
                    return 0;
                }
            };
            for (Map.Entry<Integer, List<Pair<String, WrappedArgument<?>>>> entry : arguments.entrySet()) {
                List<Pair<String, WrappedArgument<?>>> value = entry.getValue();
                ArgumentBuilder<CommandSourceStack, ?> branchArgumentBuilder = null;

                for (Pair<String, WrappedArgument<?>> stringWrappedArgumentPair : value.reversed()) {
                    WrappedArgument<?> argument = stringWrappedArgumentPair.getRight();
                    if (branchArgumentBuilder == null) {
                        branchArgumentBuilder = argument.compile().executes(executor);
                    } else {
                        branchArgumentBuilder = argument.compile().then(branchArgumentBuilder);
                        if (argument.isOptional()) {
                            branchArgumentBuilder = branchArgumentBuilder.executes(executor);
                        }
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

    private static class CustomActionNode extends ArgumentNode<String> {

        protected CustomActionNode() {
            super("custom", StringArgumentType.greedyString());
        }

        @Override
        protected boolean execute(CommandContext context) throws CommandSyntaxException {
            try {
                ServerBot bot = getBot(context);
                CommandSender sender = context.getSender();
                String[] args = StringUtils.split(context.getArgument("custom", String.class), ' ');
                CustomActionProvider provider = Actions.getCustom(args[0]);
                if (provider == null) {
                    return false;
                }
                String[] realArg = Arrays.copyOfRange(args, 1, args.length);
                ServerCustomAction action = new ServerCustomAction(provider);
                provider.loadAction(sender, realArg, (CustomAction) action.asCraft());
                if (bot.addBotAction(action, sender)) {
                    sender.sendMessage(join(spaces(),
                        text("Action", GRAY),
                        text(action.getName(), AQUA).hoverEvent(showText(text(action.getActionDataString()))),
                        text("has been issued to", GRAY),
                        asAdventure(bot.getDisplayName())
                    ));
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected CompletableFuture<Suggestions> getSuggestions(CommandContext context, SuggestionsBuilder builder) throws CommandSyntaxException {
            try {
                String[] args = builder.getRemaining().split(" ", -1);
                if (args.length <= 1) {
                    Actions.getCustomActions().forEach(builder::suggest);
                } else {
                    CustomActionProvider provider = Actions.getCustom(args[0]);
                    if (provider == null) {
                        return builder.buildFuture();
                    }
                    String[] realArg = Arrays.copyOfRange(args, 1, args.length);
                    List<String> suggestion = provider.getSuggestion(context.getSender(), realArg);
                    builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);
                    suggestion.forEach(builder::suggest);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return builder.buildFuture();
        }
    }
}
