package com.mohistmc.banner.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;

import java.util.Comparator;

public class ServerUtils {

    public static java.util.Queue<Runnable> bridge$processQueue =
            new java.util.concurrent.ConcurrentLinkedQueue<>();
    public static int bridge$autosavePeriod;

    public static final TicketType<Unit> PLUGIN = TicketType.create("plugin", (a, b) -> 0); // CraftBukkit
    public static int currentTick = (int) (System.currentTimeMillis() / 50);
    public static final TicketType<org.bukkit.plugin.Plugin> PLUGIN_TICKET =
            TicketType.create("plugin_ticket", Comparator.comparing(plugin -> plugin.getClass().getName())); // CraftBukkit
    public static final LootContextParam<Integer> LOOTING_MOD =
            new LootContextParam<>(new ResourceLocation("bukkit:looting_mod")); // CraftBukkit
    public static MinecraftServer getServer() {
        return Bukkit.getServer() instanceof CraftServer ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }

    public static int getCurrentTick() {
        return currentTick;
    }

}
