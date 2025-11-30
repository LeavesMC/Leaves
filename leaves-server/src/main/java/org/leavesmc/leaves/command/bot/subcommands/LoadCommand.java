package org.leavesmc.leaves.command.bot.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesConfig;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.ArgumentNode;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.bot.BotSubcommand;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static io.papermc.paper.adventure.PaperAdventure.asAdventure;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.spaces;

public class LoadCommand extends BotSubcommand {

    public LoadCommand() {
        super("load");
        children(BotNameArgument::new);
    }

    @Override
    public boolean requires(@NotNull CommandSourceStack source) {
        return LeavesConfig.modify.fakeplayer.canManualSaveAndLoad && super.requires(source);
    }

    private static class BotNameArgument extends ArgumentNode<String> {

        private BotNameArgument() {
            super("bot_name", StringArgumentType.word());
        }

        @Override
        protected boolean execute(@NotNull CommandContext context) throws CommandSyntaxException {
            String botName = context.getArgument(BotNameArgument.class);
            BotList botList = BotList.INSTANCE;
            CommandSender sender = context.getSender();
            if (!botList.getManualSavedBotList().contains(botName)) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
            }
            if (botList.getBotByName(botName) != null) {
                sender.sendMessage(text("Bot with name " + botName + " already exists!",  NamedTextColor.RED));
                return false;
            }

            ServerBot bot = botList.loadNewManualSavedBot(botName);
            if (bot == null) {
                sender.sendMessage(text("Failed to load bot, please check log", NamedTextColor.RED));
                return false;
            }
            sender.sendMessage(join(spaces(), text("Successfully loaded bot", NamedTextColor.GRAY), asAdventure(bot.getDisplayName())));
            return true;
        }

        @Override
        protected CompletableFuture<Suggestions> getSuggestions(CommandContext context, @NotNull SuggestionsBuilder builder) {
            BotList botList = BotList.INSTANCE;
            Set<String> bots = botList.getManualSavedBotList().keySet();
            if (bots.isEmpty()) {
                return builder
                    .suggest("<NO SAVED BOT EXISTS>", net.minecraft.network.chat.Component.literal("There are no bots saved before, save one first."))
                    .buildFuture();
            }
            bots.forEach(builder::suggest);
            return builder.buildFuture();
        }
    }
}
