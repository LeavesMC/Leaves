package org.leavesmc.leaves.util;

import com.destroystokyo.paper.PaperVersionFetcher;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.StreamSupport;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public class LeavesVersionFetcher extends PaperVersionFetcher {

    private static final Logger LOGGER = LogUtils.getClassLogger();

    private static final int DISTANCE_ERROR = -1;
    private static final int DISTANCE_UNKNOWN = -2;
    private static final String DOWNLOAD_PAGE = "https://leavesmc.org/downloads/leaves";

    @NotNull
    @Override
    public Component getVersionMessage() {
        final Component updateMessage;
        final ServerBuildInfo build = ServerBuildInfo.buildInfo();
        if (build.buildNumber().isEmpty() && build.gitCommit().isEmpty()) {
            updateMessage = text("You are running a development version without access to version information", color(0xFF5300));
        } else if (build.buildNumber().isEmpty()) {
            updateMessage = text("You are running a development version from CI", color(0xFF5300));
        } else {
            updateMessage = getUpdateStatusMessage("LeavesMC/Leaves", build);
        }
        final @Nullable Component history = this.getHistory();

        return history != null ? Component.textOfChildren(updateMessage, Component.newline(), history) : updateMessage;
    }

    private static Component getUpdateStatusMessage(@NotNull final String repo, @NotNull final ServerBuildInfo build) {
        int distance = fetchDistanceFromLeavesApiV2Build(build);

        if (distance == DISTANCE_ERROR) {
            distance = fetchDistanceFromLeavesApiV2Hash(build);
        }

        if (distance == DISTANCE_ERROR) {
            final Optional<String> gitBranch = build.gitBranch();
            final Optional<String> gitCommit = build.gitCommit();
            if (gitBranch.isPresent() && gitCommit.isPresent()) {
                distance = fetchDistanceFromGitHub(repo, gitBranch.get(), gitCommit.get());
            }
        }

        return switch (distance) {
            case DISTANCE_ERROR -> Component.text("Error obtaining version information", NamedTextColor.YELLOW);
            case 0 -> Component.text("You are running the latest version", NamedTextColor.GREEN);
            case DISTANCE_UNKNOWN -> Component.text("Unknown version", NamedTextColor.YELLOW);
            default -> Component.text("You are " + distance + " version(s) behind", NamedTextColor.YELLOW)
                .append(Component.newline())
                .append(Component.text("Download the new version at: ")
                    .append(Component.text(DOWNLOAD_PAGE, NamedTextColor.GOLD)
                        .hoverEvent(Component.text("Click to open", NamedTextColor.WHITE))
                        .clickEvent(ClickEvent.openUrl(DOWNLOAD_PAGE))));
        };
    }

    private static int fetchDistanceFromLeavesApiV2Build(final ServerBuildInfo build) {
        OptionalInt buildNumber = build.buildNumber();
        if (buildNumber.isEmpty()) {
            return DISTANCE_ERROR;
        }

        try {
            try (final BufferedReader reader = Resources.asCharSource(
                URI.create("https://api.leavesmc.org/v2/projects/leaves/versions/" + build.minecraftVersionId()).toURL(),
                Charsets.UTF_8
            ).openBufferedStream()) {
                final JsonObject json = new Gson().fromJson(reader, JsonObject.class);
                final JsonArray builds = json.getAsJsonArray("builds");
                final int latest = StreamSupport.stream(builds.spliterator(), false)
                    .mapToInt(JsonElement::getAsInt)
                    .max()
                    .orElseThrow();
                return latest - buildNumber.getAsInt();
            } catch (final JsonSyntaxException ex) {
                LOGGER.error("Error parsing json from Leaves's downloads API", ex);
                return DISTANCE_ERROR;
            }
        } catch (final IOException e) {
            LOGGER.error("Error while parsing version", e);
            return DISTANCE_ERROR;
        }
    }

    private static int fetchDistanceFromLeavesApiV2Hash(final ServerBuildInfo build) {
        if (build.gitCommit().isEmpty()) {
            return DISTANCE_ERROR;
        }

        try {
            try (BufferedReader reader = Resources.asCharSource(
                URI.create("https://api.leavesmc.org/v2/projects/leaves/versions/" + build.minecraftVersionId() + "/differ/" + build.gitCommit().get()).toURL(),
                Charsets.UTF_8
            ).openBufferedStream()) {
                return Integer.parseInt(reader.readLine());
            }
        } catch (IOException e) {
            LOGGER.error("Error while parsing version", e);
            return DISTANCE_ERROR;
        }
    }
}
