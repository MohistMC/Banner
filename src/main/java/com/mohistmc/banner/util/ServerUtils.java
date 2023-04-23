package com.mohistmc.banner.util;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;

public class ServerUtils {

    public static CommandDispatcher bridge$vanillaCommandDispatcher;
    public static java.util.Queue<Runnable> bridge$processQueue =
            new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();
    public static int bridge$autosavePeriod;

    public static MinecraftServer getServer() {
        return Bukkit.getServer() instanceof CraftServer ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }

    public static int getCurrentTick() {
        return (int) (System.currentTimeMillis() / 50);
    }
}
