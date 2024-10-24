package org.bukkit.craftbukkit;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.util.PathConverter;
import net.minecrell.terminalconsole.TerminalConsoleAppender;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

public class Main extends OptionParser {

    public static boolean useJline = true;
    public static boolean useConsole = true;

    public Main() {

        acceptsAll(asList("?", "help"), "Show the help");

        acceptsAll(asList("c", "config"), "Properties file to use")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("server.properties"))
                .describedAs("Properties file");

        acceptsAll(asList("P", "plugins"), "Plugin directory to use")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("plugins"))
                .describedAs("Plugin directory");

        acceptsAll(asList("h", "host", "server-ip"), "Host to listen on")
                .withRequiredArg()
                .ofType(String.class)
                .describedAs("Hostname or IP");

        acceptsAll(asList("W", "world-dir", "universe", "world-container"), "World container")
                .withRequiredArg()
                .ofType(File.class)
                .describedAs("Directory containing worlds");

        acceptsAll(asList("w", "world", "level-name"), "World name")
                .withRequiredArg()
                .ofType(String.class)
                .describedAs("World name");

        acceptsAll(asList("p", "port", "server-port"), "Port to listen on")
                .withRequiredArg()
                .ofType(Integer.class)
                .describedAs("Port");

        accepts("serverId", "Server ID")
                .withRequiredArg();

        accepts("jfrProfile", "Enable JFR profiling");

        accepts("pidFile", "pid File")
                .withRequiredArg()
                .withValuesConvertedBy(new PathConverter());

        acceptsAll(asList("o", "online-mode"), "Whether to use online authentication")
                .withRequiredArg()
                .ofType(Boolean.class)
                .describedAs("Authentication");

        acceptsAll(asList("s", "size", "max-players"), "Maximum amount of players")
                .withRequiredArg()
                .ofType(Integer.class)
                .describedAs("Server size");

        acceptsAll(asList("d", "date-format"), "Format of the date to display in the console (for log entries)")
                .withRequiredArg()
                .ofType(SimpleDateFormat.class)
                .describedAs("Log date format");

        acceptsAll(asList("log-pattern"), "Specfies the log filename pattern")
                .withRequiredArg()
                .ofType(String.class)
                .defaultsTo("server.log")
                .describedAs("Log filename");

        acceptsAll(asList("log-limit"), "Limits the maximum size of the log file (0 = unlimited)")
                .withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(0)
                .describedAs("Max log size");

        acceptsAll(asList("log-count"), "Specified how many log files to cycle through")
                .withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(1)
                .describedAs("Log count");

        acceptsAll(asList("log-append"), "Whether to append to the log file")
                .withRequiredArg()
                .ofType(Boolean.class)
                .defaultsTo(true)
                .describedAs("Log append");

        acceptsAll(asList("log-strip-color"), "Strips color codes from log file");

        acceptsAll(asList("b", "bukkit-settings"), "File for bukkit settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("bukkit.yml"))
                .describedAs("Yml file");

        acceptsAll(asList("C", "commands-settings"), "File for command settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("commands.yml"))
                .describedAs("Yml file");

        acceptsAll(asList("forceUpgrade"), "Whether to force a world upgrade");
        acceptsAll(asList("eraseCache"), "Whether to force cache erase during world upgrade");

        acceptsAll(asList("nojline"), "Disables jline and emulates the vanilla console");

        acceptsAll(asList("noconsole"), "Disables the console");
        acceptsAll(asList("v", "version"), "Show the CraftBukkit Version");

        acceptsAll(asList("demo"), "Demo mode");

        // Spigot Start
        acceptsAll(asList("S", "spigot-settings"), "File for spigot settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("spigot.yml"))
                .describedAs("Yml file");

        // Banner Start
        acceptsAll(asList("B", "banner-settings"), "File for banner settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("banner-config","banner.yml"))
                .describedAs("Yml file");

        allowsUnrecognizedOptions();
    }

    public static void handleParser(OptionParser parser, OptionSet options) {
        if ((options == null) || (options.has("?"))) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (options.has("v")) {
            System.out.println(CraftServer.class.getPackage().getImplementationVersion());
        } else {
            // Do you love Java using + and ! as string based identifiers? I sure do!
            String path = new File(".").getAbsolutePath();
            if (path.contains("!") || path.contains("+")) {
                System.err.println("Cannot run server in a directory with ! or + in the pathname. Please rename the affected folders and try again.");
                return;
            }

            float javaVersion = Float.parseFloat(System.getProperty("java.class.version"));
            if (javaVersion < 61.0) {
                System.err.println("Unsupported Java detected (" + javaVersion + "). This version of Minecraft requires at least Java 17. Check your Java version with the command 'java -version'.");
                return;
            }
            if (javaVersion > 65.0) {
                System.err.println("Unsupported Java detected (" + javaVersion + "). Only up to Java 21 is supported.");
                return;
            }

            String javaVersionName = System.getProperty("java.version");
            // J2SE SDK/JRE Version String Naming Convention
            boolean isPreRelease = javaVersionName.contains("-");
            if (isPreRelease && javaVersion == 61.0) {
                System.err.println("Unsupported Java detected (" + javaVersionName + "). You are running an outdated, pre-release version. Only general availability versions of Java are supported. Please update your Java version.");
                return;
            }

            if (options.has("nojline")) {
                System.setProperty(TerminalConsoleAppender.JLINE_OVERRIDE_PROPERTY, "false");
                useJline = false;
            }

            if (options.has("noconsole")) {
                useConsole = false;
                useJline = false;
                System.setProperty(TerminalConsoleAppender.JLINE_OVERRIDE_PROPERTY, "false");
            }
        }
    }
}
