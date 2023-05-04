package org.bukkit.plugin;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

/**
 * The PluginLogger class is a modified {@link Logger} that prepends all
 * logging calls with the name of the plugin doing the logging. The API for
 * PluginLogger is exactly the same as {@link Logger}.
 *
 * @see Logger
 */
public class PluginLogger extends Logger {

    private String pluginName;
    public org.apache.logging.log4j.Logger log4j;

    /**
     * Creates a new PluginLogger that extracts the name from a plugin.
     *
     * @param context A reference to the plugin
     */
    public PluginLogger(@NotNull Plugin context) {
        super(context.getClass().getCanonicalName(), null);
        String prefix = context.getDescription().getPrefix();
        pluginName = prefix != null ? new StringBuilder().append("[").append(prefix).append("] ").toString() : "[" + context.getDescription().getName() + "] ";
        setParent(context.getServer().getLogger());
        setLevel(Level.ALL);
        this.log4j = LogManager.getLogger("Bukkit");
    }

    @Override
    public void log(@NotNull LogRecord logRecord) {
        //logRecord.setMessage(pluginName + logRecord.getMessage());
        if (logRecord.getThrown() == null)
            log4j.log(convertLevel(logRecord.getLevel()), logRecord.getMessage());
        else log4j.log(convertLevel(logRecord.getLevel()), logRecord.getMessage(), logRecord.getThrown());
        super.log(logRecord);
    }

    private org.apache.logging.log4j.Level convertLevel(java.util.logging.Level l) {
        if (l == java.util.logging.Level.ALL)     return org.apache.logging.log4j.Level.ALL;
        if (l == java.util.logging.Level.CONFIG)  return org.apache.logging.log4j.Level.TRACE;
        if (l == java.util.logging.Level.WARNING) return org.apache.logging.log4j.Level.WARN;
        if (l == java.util.logging.Level.INFO)    return org.apache.logging.log4j.Level.INFO;
        if (l == java.util.logging.Level.OFF)     return org.apache.logging.log4j.Level.OFF;
        if (l == java.util.logging.Level.SEVERE)  return org.apache.logging.log4j.Level.FATAL;

        if (l == java.util.logging.Level.FINE || l == java.util.logging.Level.FINER || l == java.util.logging.Level.FINEST)
            return org.apache.logging.log4j.Level.WARN;
        return org.apache.logging.log4j.Level.ALL;
    }
}
