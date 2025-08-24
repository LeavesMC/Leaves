package org.leavesmc.leaves.neo_command.bot.subcommands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.event.bot.BotRemoveEvent;
import org.leavesmc.leaves.neo_command.CommandContext;
import org.leavesmc.leaves.neo_command.CustomArgumentNode;
import org.leavesmc.leaves.neo_command.bot.BotSubcommand;

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

    private static class BotArgument extends CustomArgumentNode<ServerBot, String> {

        private BotArgument() {
            super("bot", new org.leavesmc.leaves.neo_command.bot.BotArgument());
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) throws CommandSyntaxException {
            ServerBot bot = context.getCustomArgument(BotArgument.class);
            CommandSender sender = context.getSender();
            BotList botList = BotList.INSTANCE;

            boolean success = botList.removeBot(bot, BotRemoveEvent.RemoveReason.COMMAND, sender, true);
            if (success) {
                sender.sendMessage(join(spaces(),
                    text("Successfully saved bot", NamedTextColor.GRAY),
                    asAdventure(bot.getDisplayName()),
                    text("as " + bot.createState.realName(), NamedTextColor.GRAY)
                ));
            } else {
                sender.sendMessage(text("Bot save canceled by a plugin", NamedTextColor.RED));
            }
            return success;
        }
    }
}
