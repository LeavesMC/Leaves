package org.leavesmc.leaves.command.bot;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;
import org.leavesmc.leaves.bot.BotList;
import org.leavesmc.leaves.bot.ServerBot;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.CustomArgumentType;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class BotArgument implements CustomArgumentType<ServerBot, String> {
    @Override
    public ArgumentType<String> getBaseArgumentType() {
        return StringArgumentType.word();
    }

    @Override
    public ServerBot transform(String value) throws CommandSyntaxException {
        ServerBot bot = BotList.INSTANCE.getBotByName(value);
        if (bot == null) {
            throw new CommandSyntaxException(
                CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(),
                Component.literal("Bot with name '" + value + "' does not exist")
            );
        }
        return bot;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext context, SuggestionsBuilder builder) throws CommandSyntaxException {
        Collection<ServerBot> bots = BotList.INSTANCE.bots;
        if (bots.isEmpty()) {
            return builder
                .suggest("<NO BOT EXISTS>", net.minecraft.network.chat.Component.literal("There are no bots in the server, create one first."))
                .buildFuture();
        }
        bots.stream().map(ServerBot::getScoreboardName).forEach(builder::suggest);
        return builder.buildFuture();
    }
}
