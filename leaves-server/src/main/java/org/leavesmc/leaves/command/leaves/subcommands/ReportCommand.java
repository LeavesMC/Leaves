package org.leavesmc.leaves.command.leaves.subcommands;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.entrypoint.Entrypoint;
import io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.provider.type.paper.PaperPluginParent;
import io.papermc.paper.plugin.provider.type.spigot.SpigotPluginProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.flag.FeatureFlagSet;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.command.CommandContext;
import org.leavesmc.leaves.command.LiteralNode;
import org.leavesmc.leaves.command.leaves.LeavesSubcommand;
import org.leavesmc.leaves.plugin.provider.configuration.LeavesPluginMeta;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.JoinConfiguration.spaces;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;

public class ReportCommand extends LeavesSubcommand {
    private static final String BASE_URL = "https://github.com/LeavesMC/Leaves/issues/new?template=";
    private static final String BUG_REPORT_URL = BASE_URL + "1-bug-report.yml&leaves-version=${version}&plugin-list=${plugins}%0a%0a${datapacks}";
    private static final String NOT_VANILLA_URL = BASE_URL + "2-not-vanilla.yml&leaves-version=${version}";

    public ReportCommand() {
        super("report");
        children(
            () -> new ReportTypeNode("bug-report", BUG_REPORT_URL),
            () -> new ReportTypeNode("not-vanilla", NOT_VANILLA_URL)
        );
    }

    private static class ReportTypeNode extends LiteralNode {
        private final String url;

        private ReportTypeNode(String type, String url) {
            super(type);
            this.url = url;
        }

        @Override
        protected boolean execute(CommandContext context) {
            CompletableFuture.runAsync(() -> sendOnSuccess(context.getSender(), url, name));
            return true;
        }
    }

    private static @NotNull String generatePluginMessage() {
        final StringBuilder pluginList = new StringBuilder();

        final TreeMap<String, PluginProvider<JavaPlugin>> paperPlugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        final TreeMap<String, PluginProvider<JavaPlugin>> spigotPlugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        final TreeMap<String, PluginProvider<JavaPlugin>> leavesPlugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (final PluginProvider<JavaPlugin> provider : LaunchEntryPointHandler.INSTANCE.get(Entrypoint.PLUGIN).getRegisteredProviders()) {
            final PluginMeta configuration = provider.getMeta();

            if (provider instanceof SpigotPluginProvider) {
                spigotPlugins.put(configuration.getDisplayName(), provider);
            } else if (provider instanceof PaperPluginParent.PaperServerPluginProvider) {
                if (provider.getMeta() instanceof LeavesPluginMeta) {
                    leavesPlugins.put(configuration.getDisplayName(), provider);
                } else {
                    paperPlugins.put(configuration.getDisplayName(), provider);
                }
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
            pluginList.deleteCharAt(pluginList.length() - 1).append("\n");
        }

        if (!paperPlugins.isEmpty()) {
            pluginList.append("Paper Plugins (%s):".formatted(paperPlugins.size())).append("\n").append(" -");
            for (final PluginProvider<?> entry : paperPlugins.values()) {
                pluginList.append(" ").append(entry.getMeta().getName()).append(",");
            }
            pluginList.deleteCharAt(pluginList.length() - 1).append("\n");
        }

        if (!spigotPlugins.isEmpty()) {
            pluginList.append("Bukkit Plugins (%s):".formatted(spigotPlugins.size())).append("\n").append(" -");
            for (final PluginProvider<?> entry : spigotPlugins.values()) {
                pluginList.append(" ").append(entry.getMeta().getAPIVersion() == null ? "*" : "").append(entry.getMeta().getName()).append(",");
            }
            pluginList.deleteCharAt(pluginList.length() - 1).append("\n");
        }

        return pluginList.toString();
    }

    private static @NotNull String generateDataPackMessage() {
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

    private static void sendOnSuccess(@NotNull CommandSender sender, @NotNull String template, String type) {
        String finalUrl = template
            .replace("${version}", URLEncoder.encode(Bukkit.getVersionMessage(), StandardCharsets.UTF_8))
            .replace("${plugins}", URLEncoder.encode(generatePluginMessage(), StandardCharsets.UTF_8))
            .replace("${datapacks}", URLEncoder.encode(generateDataPackMessage(), StandardCharsets.UTF_8));
        Component base = join(noSeparators(),
            text("Successfully generated report url for ", GRAY),
            text(type, AQUA),
            text(",", GRAY)
        );
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(join(spaces(),
                base,
                text("please open it in your browser:", GRAY),
                text(finalUrl, AQUA)
            ));
        } else {
            sender.sendMessage(join(spaces(),
                base,
                text("click here to continue", AQUA)
                    .decorate(UNDERLINED)
                    .hoverEvent(text("Open the report url"))
                    .clickEvent(ClickEvent.openUrl(finalUrl))
            ));
        }
    }
}