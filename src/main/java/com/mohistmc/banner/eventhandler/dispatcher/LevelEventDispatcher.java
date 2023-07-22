package com.mohistmc.banner.eventhandler.dispatcher;

import com.mohistmc.banner.fabric.FabricHookBukkitEvent;
import com.mohistmc.banner.fabric.FabricInjectBukkit;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.event.world.WorldInitEvent;

public class LevelEventDispatcher {

    public static void dispatchLevel() {
        FabricHookBukkitEvent.EVENT.register(bukkitEvent -> {
            if (bukkitEvent instanceof WorldInitEvent) {
                FabricInjectBukkit.addEnumEnvironment(); // Banner
            }
        });
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            ((CraftServer) Bukkit.getServer()).removeWorld(world);
        });
    }
}
