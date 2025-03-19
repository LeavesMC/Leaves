package org.leavesmc.leaves.command.subcommands;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.entrypoint.Entrypoint;
import io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.provider.type.paper.PaperPluginParent;
import io.papermc.paper.plugin.provider.type.spigot.SpigotPluginProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.flag.FeatureFlagSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.leavesmc.leaves.command.LeavesCommandUtil;
import org.leavesmc.leaves.command.LeavesSubcommand;
import org.leavesmc.leaves.plugin.MinecraftInternalPlugin;
import org.leavesmc.leaves.plugin.provider.configuration.LeavesPluginMeta;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class ReportCommand implements LeavesSubcommand {

    private static final String BUG_REPORT_URL = "https://github.com/LeavesMC/Leaves/issues/new?template=1-bug-report.yml&leaves-version=${version}&plugin-list=${plugins}%0a%0a${datapacks}";
    private static final String NOT_VANILLA_URL = "https://github.com/LeavesMC/Leaves/issues/new?template=2-not-vanilla.yml&leaves-version=${version}";
    private static final String COMMAND_PERM = "bukkit.command.leaves.report";

    @Override
    public boolean execute(CommandSender sender, String subCommand, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(text("Please select a report template: \"bug-report\" or \"not-vanilla\"", RED));
            return true;
        }
        if (args[0].equals("bug-report")) {
            Bukkit.getScheduler().runTaskAsynchronously(MinecraftInternalPlugin.INSTANCE, () -> {
                sendOnSuccess(sender, BUG_REPORT_URL, Component.text("Successfully generated report url for \"bug-report\"", AQUA));
            });
            return true;
        } else if (args[0].equals("not-vanilla")) {
            Bukkit.getScheduler().runTaskAsynchronously(MinecraftInternalPlugin.INSTANCE, () -> {
                sendOnSuccess(sender, NOT_VANILLA_URL, Component.text("Successfully generated report url for \"not-vanilla\"", AQUA));
            });
            return true;
        }
        sender.sendMessage(text("The template" + args[0] + " does not exist! Please select a correct template: \"bug-report\" or \"not-vanilla\"", RED));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String[] args, Location location) {
        if (args.length <= 1) {
            return LeavesCommandUtil.getListMatchingLast(sender, args, List.of("bug-report", "not-vanilla"), COMMAND_PERM + ".", COMMAND_PERM);
        }
        return Collections.emptyList();
    }

    private void sendOnSuccess(CommandSender sender, String template, Component component) {
        String finalUrl = template
            .replace("${version}", URLEncoder.encode(Bukkit.getVersionMessage(), StandardCharsets.UTF_8))
            .replace("${plugins}", URLEncoder.encode(generatePluginMessage(), StandardCharsets.UTF_8))
            .replace("${datapacks}", URLEncoder.encode(generateDataPackMessage(), StandardCharsets.UTF_8));
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(component.append(text(", please copy it as you are running this command in console")));
            sender.sendMessage(text(finalUrl, AQUA).decorate(TextDecoration.UNDERLINED));
        } else {
            sender.sendMessage(component.append(text(", click this message to open")).decorate(TextDecoration.UNDERLINED).hoverEvent(Component.text("Click to open the report url", NamedTextColor.WHITE)).clickEvent(ClickEvent.openUrl(finalUrl)));
        }
    }

    private static String generatePluginMessage() {
        final StringBuilder pluginList = new StringBuilder();

        final TreeMap<String, PluginProvider<JavaPlugin>> paperPlugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        final TreeMap<String, PluginProvider<JavaPlugin>> spigotPlugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        final TreeMap<String, PluginProvider<JavaPlugin>> leavesPlugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (final PluginProvider<JavaPlugin> provider : LaunchEntryPointHandler.INSTANCE.get(Entrypoint.PLUGIN).getRegisteredProviders()) {
            final PluginMeta configuration = provider.getMeta();

            if (provider instanceof SpigotPluginProvider) {
                spigotPlugins.put(configuration.getDisplayName(), provider);
            } else if (provider instanceof PaperPluginParent.PaperServerPluginProvider) {
                if (provider.getMeta() instanceof LeavesPluginMeta) leavesPlugins.put(configuration.getDisplayName(), provider);
                else paperPlugins.put(configuration.getDisplayName(), provider);
            }
        }

        final int sizeLeavesPlugins = leavesPlugins.size();
        final int sizePaperPlugins = paperPlugins.size();
        final int sizeSpigotPlugins = spigotPlugins.size();
        final int sizePlugins = sizePaperPlugins + sizeSpigotPlugins + sizeLeavesPlugins;

        pluginList.append("Server Plugins (%s):".formatted(sizePlugins)).append("\n");

        if (!leavesPlugins.isEmpty()) {
            pluginList.append("Leaves Plugins (%s):".formatted(leavesPlugins.size())).append("\n").append(" -");
            for (final PluginProvider<?> entry : leavesPlugins.values()) {
                pluginList.append(" ").append(entry.getMeta().getName()).append(",");
            }
            pluginList.deleteCharAt(pluginList.length() - 1);
        }

        if (!paperPlugins.isEmpty()) {
            pluginList.append("Paper Plugins (%s):".formatted(paperPlugins.size())).append("\n").append(" -");
            for (final PluginProvider<?> entry : paperPlugins.values()) {
                pluginList.append(" ").append(entry.getMeta().getName()).append(",");
            }
            pluginList.deleteCharAt(pluginList.length() - 1);
        }

        if (!spigotPlugins.isEmpty()) {
            pluginList.append("Bukkit Plugins (%s):".formatted(spigotPlugins.size())).append("\n").append(" -");
            for (final PluginProvider<?> entry : spigotPlugins.values()) {
                pluginList.append(" ").append(entry.getMeta().getAPIVersion() == null ? "*" : "").append(entry.getMeta().getName()).append(",");
            }
            pluginList.deleteCharAt(pluginList.length() - 1);
        }

        return pluginList.toString();
    }

    private static String generateDataPackMessage() {
        final StringBuilder dataPackList = new StringBuilder();

        PackRepository packRepository = MinecraftServer.getServer().getPackRepository();
        packRepository.reload();
        Collection<? extends Pack> selectedPacks = packRepository.getSelectedPacks();
        if (selectedPacks.isEmpty()) {
            dataPackList.append("There are no data packs enabled");
        } else {
            dataPackList.append("There are %s data pack(s) enabled:".formatted(selectedPacks.size()));
            for (Pack pack : selectedPacks) {
                dataPackList.append(" ").append(pack.getChatLink(false).getString()).append(",");
            }
            dataPackList.deleteCharAt(dataPackList.length() - 1).append("\n");
        }

        Collection<Pack> availablePacks = packRepository.getAvailablePacks();
        FeatureFlagSet featureFlagSet = MinecraftServer.getServer().getWorldData().enabledFeatures();
        List<Pack> list = availablePacks.stream()
            .filter(pack -> !selectedPacks.contains(pack) && pack.getRequestedFeatures().isSubsetOf(featureFlagSet))
            .toList();
        if (list.isEmpty()) {
            dataPackList.append("There are no more data packs available");
        } else {
            dataPackList.append("There are %s data pack(s) available:".formatted(selectedPacks.size()));
            for (Pack pack : list) {
                dataPackList.append(" ").append(pack.getChatLink(false).getString()).append(",");
            }
        }
        return dataPackList.toString();
    }
}