package com.mohistmc.banner.eventhandler.dispatcher;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;

public class LevelEventDispatcher {

    public static void dispatchLevel() {
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            ((CraftServer) Bukkit.getServer()).removeWorld(world);
        });
    }
}
