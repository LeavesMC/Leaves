package org.leavesmc.leaves.spark;

import me.lucko.spark.paper.PaperSparkPlugin;
import me.lucko.spark.paper.api.Compatibility;
import me.lucko.spark.paper.api.PaperClassLookup;
import me.lucko.spark.paper.api.PaperScheduler;
import me.lucko.spark.paper.common.platform.serverconfig.ServerConfigProvider;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class LeavesSparkPlugin extends PaperSparkPlugin {

    @NotNull
    public static LeavesSparkPlugin create(Compatibility ignoredCompatibility, Server server, Logger logger, PaperScheduler scheduler, PaperClassLookup classLookup) {
        return new LeavesSparkPlugin(server, logger, scheduler, classLookup);
    }

    public LeavesSparkPlugin(Server server, Logger logger, PaperScheduler scheduler, PaperClassLookup classLookup) {
        super(server, logger, scheduler, classLookup);
    }

    @Override
    public ServerConfigProvider createServerConfigProvider() {
        return new LeavesServerConfigProvider();
    }
}
