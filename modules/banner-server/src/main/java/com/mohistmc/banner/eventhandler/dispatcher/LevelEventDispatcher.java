package com.mohistmc.banner.eventhandler.dispatcher;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;

import java.util.Locale;

public class LevelEventDispatcher {

    public static void dispatchLevel() {
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            removeWorld(world);
        });
    }

    public static void removeWorld(ServerLevel world) {
        if (world == null) {
            return;
        }
        ((CraftServer) Bukkit.getServer()).getWorlds().remove(world.getWorld().getName().toLowerCase(Locale.ROOT));// Banner - use Root instead of English
    }
}
