package com.mohistmc.banner.util;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;

public class ServerUtils {

    public static CommandDispatcher bridge$vanillaCommandDispatcher;
    public static java.util.Queue<Runnable> bridge$processQueue =
            new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();
    public static int bridge$autosavePeriod;

    public static final TicketType<Unit> PLUGIN = TicketType.create("plugin", (a, b) -> 0); // CraftBukkit
    public static final TicketType<org.bukkit.plugin.Plugin> PLUGIN_TICKET =
            TicketType.create("plugin_ticket", (plugin1, plugin2) -> plugin1.getClass().getName().compareTo(plugin2.getClass().getName())); // CraftBukkit

    public static MinecraftServer getServer() {
        return Bukkit.getServer() instanceof CraftServer ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }

    public static int getCurrentTick() {
        return (int) (System.currentTimeMillis() / 50);
    }
}
