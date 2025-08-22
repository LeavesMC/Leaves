package org.leavesmc.leaves.neo_command.bot.subcommands.action;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.bot.agent.Actions;
import org.leavesmc.leaves.bot.agent.actions.ServerBotAction;
import org.leavesmc.leaves.neo_command.CommandContext;
import org.leavesmc.leaves.neo_command.LiteralNode;
import org.leavesmc.leaves.neo_command.WrappedArgument;

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
import static org.leavesmc.leaves.neo_command.bot.subcommands.ActionCommand.BotArgument.getBot;

public class StartCommand extends LiteralNode {

    public StartCommand() {
        super("start");
        Actions.getAll()
            .stream()
            .map(this::actionNodeCreator)
            .forEach(this::children);
    }

    private boolean executor(CommandContext context, @NotNull ServerBotAction<?> action) throws CommandSyntaxException {
        ServerBot bot = getBot(context);
        CommandSender sender = context.getSender();
        if (bot == null) {
            return false;
        }
        action.loadCommand(context);
        if (bot.addBotAction(action, sender)) {
            sender.sendMessage(join(spaces(),
                text("Action", GRAY),
                text(action.getName(), AQUA).hoverEvent(showText(text(action.getReadableActionDataString()))),
                text("has been issued to", GRAY),
                asAdventure(bot.getDisplayName())
            ));
        }
        return true;
    }

    @Contract(pure = true)
    private @NotNull Supplier<LiteralNode> actionNodeCreator(ServerBotAction<?> action) {
        return () -> new LiteralNode(action.getName()) {
            @Override
            protected ArgumentBuilder<CommandSourceStack, ?> compile() {
                ArgumentBuilder<CommandSourceStack, ?> builder = super.compile();

                for (Map.Entry<Integer, List<Pair<String, WrappedArgument<?>>>> entry : action.getArguments().entrySet()) {
                    List<Pair<String, WrappedArgument<?>>> value = entry.getValue();
                    ArgumentBuilder<CommandSourceStack, ?> branchArgumentBuilder = null;
                    Command<CommandSourceStack> executor = context -> {
                        if (executor(new CommandContext(context), action)) {
                            return Command.SINGLE_SUCCESS;
                        } else {
                            return 0;
                        }
                    };

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

                    if (value.getFirst().getRight().isOptional()) {
                        builder = builder.executes(executor);
                    }

                    if (branchArgumentBuilder != null) {
                        builder = builder.then(branchArgumentBuilder);
                    }
                }
                return builder;
            }
        };
    }
}
