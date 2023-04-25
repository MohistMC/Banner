package com.mohistmc.banner.mixin.server;

import com.mohistmc.banner.injection.server.InjectionMinecraftServer;
import com.mohistmc.banner.util.ServerUtils;
import com.mojang.brigadier.CommandDispatcher;
import jline.console.ConsoleReader;
import joptsimple.OptionSet;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.WorldLoader;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements InjectionMinecraftServer {

    // CraftBukkit start
    public WorldLoader.DataLoadContext worldLoader;
    public org.bukkit.craftbukkit.v1_19_R3.CraftServer server;
    public OptionSet options;
    public org.bukkit.command.ConsoleCommandSender console;
    public org.bukkit.command.RemoteConsoleCommandSender remoteConsole;
    public ConsoleReader reader;
    private static int currentTick = ServerUtils.getCurrentTick();
    public java.util.Queue<Runnable> processQueue = ServerUtils.bridge$processQueue;
    public int autosavePeriod = ServerUtils.bridge$autosavePeriod;
    private boolean forceTicks;
    public CommandDispatcher vanillaCommandDispatcher = ServerUtils.bridge$vanillaCommandDispatcher;
    // CraftBukkit end

    public MixinMinecraftServer(String string) {
        super(string);
    }

    /**
     * @author 1798643961
     * @reason our branding
     */
    @Overwrite(remap = false)
    public String getServerModName() {
        return "banner";
    }

    private static MinecraftServer getServer() {
        return ServerUtils.getServer();
    }

    // Banner start
    @Override
    public WorldLoader.DataLoadContext bridge$worldLoader() {
        return worldLoader;
    }

    @Override
    public CraftServer bridge$server() {
        return server;
    }

    @Override
    public OptionSet bridge$options() {
        return options;
    }

    @Override
    public ConsoleCommandSender bridge$console() {
        return console;
    }

    @Override
    public RemoteConsoleCommandSender bridge$remoteConsole() {
        return remoteConsole;
    }

    @Override
    public ConsoleReader bridge$reader() {
        return reader;
    }

    @Override
    public boolean bridge$forceTicks() {
        return forceTicks;
    }

    @Override
    public boolean isDebugging() {
        return false;
    }

    // Banner end
}
