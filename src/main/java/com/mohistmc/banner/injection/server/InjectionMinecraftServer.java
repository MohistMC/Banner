package com.mohistmc.banner.injection.server;

import jline.console.ConsoleReader;
import joptsimple.OptionSet;
import net.minecraft.server.WorldLoader;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;

public interface InjectionMinecraftServer {

    default WorldLoader.DataLoadContext bridge$worldLoader() {
        return null;
    }

    default CraftServer bridge$server() {
        return null;
    }

    default OptionSet bridge$options() {
        return null;
    }

    default ConsoleCommandSender bridge$console() {
        return null;
    }

    default RemoteConsoleCommandSender bridge$remoteConsole() {
        return null;
    }

    default ConsoleReader bridge$reader() {
        return null;
    }

    default boolean bridge$forceTicks() {
        return false;
    }

    default boolean isDebugging() {
        return false;
    }
}
