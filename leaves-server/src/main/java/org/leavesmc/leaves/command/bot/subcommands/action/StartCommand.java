package org.leavesmc.leaves.command.bot.subcommands.action;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.actions.AbstractBotAction;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.LiteralNode;
import org.leavesmc.leaves.command.WrappedArgument;

import java.util.List;
import java.util.Map;
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
    }

    private boolean handleStartCommand(CommandContext context, @NotNull Actions<?> holder) throws CommandSyntaxException {
        ServerBot bot = getBot(context);
        CommandSender sender = context.getSender();

        AbstractBotAction<?> action = holder.createAndLoad(context);
        if (bot.addBotAction(action, sender)) {
            sender.sendMessage(join(spaces(),
                text("Action", GRAY),
                text(holder.getName(), AQUA).hoverEvent(showText(text(action.getActionDataString()))),
                text("has been issued to", GRAY),
                asAdventure(bot.getDisplayName())
            ));
        }

        return true;
    }

    @Contract(pure = true)
    private @NotNull Supplier<LiteralNode> actionNodeCreator(Actions<?> action) {
        return () -> new ActionLiteralNode(action);
    }

    private class ActionLiteralNode extends LiteralNode {
        private final Actions<?> holder;

        private ActionLiteralNode(@NotNull Actions<?> holder) {
            super(holder.getName());
            this.holder = holder;
        }

        @Override
        protected ArgumentBuilder<CommandSourceStack, ?> compile() {
            ArgumentBuilder<CommandSourceStack, ?> builder = super.compile();

            Map<Integer, List<Pair<String, WrappedArgument<?>>>> arguments = holder.create().getArguments();
            Command<CommandSourceStack> executor = context -> {
                if (handleStartCommand(new CommandContext(context), holder)) {
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
}
