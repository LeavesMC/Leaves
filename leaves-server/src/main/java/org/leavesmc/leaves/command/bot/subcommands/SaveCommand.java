package org.leavesmc.leaves.command.bot.subcommands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.ArgumentNode;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.arguments.BotArgumentType;
import org.leavesmc.leaves.command.bot.BotSubcommand;
import org.leavesmc.leaves.event.bot.BotRemoveEvent;

import static io.papermc.paper.adventure.PaperAdventure.asAdventure;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.spaces;

public class SaveCommand extends BotSubcommand {

    public SaveCommand() {
        super("save");
        children(BotArgument::new);
    }

    @Override
    public boolean requires(@NotNull CommandSourceStack source) {
        return LeavesConfig.modify.fakeplayer.canManualSaveAndLoad && super.requires(source);
    }

    private static class BotArgument extends ArgumentNode<ServerBot> {

        private BotArgument() {
            super("bot", BotArgumentType.bot());
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) {
            ServerBot bot = context.getArgument(BotArgument.class);
            CommandSender sender = context.getSender();
            BotList botList = BotList.INSTANCE;

            boolean success = botList.removeBot(bot, BotRemoveEvent.RemoveReason.COMMAND, sender, true, false);
            if (success) {
                sender.sendMessage(join(spaces(),
                    text("Successfully saved bot", NamedTextColor.GRAY),
                    asAdventure(bot.getDisplayName()),
                    text("as " + bot.createState.fullName(), NamedTextColor.GRAY)
                ));
            } else {
                sender.sendMessage(text("Bot save canceled by a plugin", NamedTextColor.RED));
            }
            return success;
        }
    }
}
