package org.leavesmc.leaves;

import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

public class LeavesLogger extends java.util.logging.Logger {

    public static final LeavesLogger LOGGER = new LeavesLogger();
    public static final Logger SLF4JLogger = LoggerFactory.getLogger(LOGGER.getName());

    private LeavesLogger() {
        super("Leaves", null);
        setParent(Bukkit.getLogger());
        setLevel(Level.ALL);
    }

    public void severe(String msg, Exception exception) {
        this.log(Level.SEVERE, msg, exception);
    }

    public void warning(String msg, Exception exception) {
        this.log(Level.WARNING, msg, exception);
    }
}