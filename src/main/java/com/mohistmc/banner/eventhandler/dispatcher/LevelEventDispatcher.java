package com.mohistmc.banner.eventhandler.dispatcher;

import com.mohistmc.banner.fabric.FabricHookBukkitEvent;
import com.mohistmc.banner.fabric.FabricInjectBukkit;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.scoreboard.CraftScoreboardManager;
import org.bukkit.event.world.WorldInitEvent;

public class LevelEventDispatcher {

    public static void dispatchLevel() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (((CraftServer) Bukkit.getServer()).scoreboardManager == null) {
                ((CraftServer) Bukkit.getServer()).scoreboardManager = new CraftScoreboardManager(server, world.getScoreboard());
            }
            if (world.bridge$generator() != null) {
                world.getWorld().getPopulators().addAll(
                        world.bridge$generator().getDefaultPopulators(
                                world.getWorld()));
            }
        });
        FabricHookBukkitEvent.EVENT.register(bukkitEvent -> {
            if (bukkitEvent instanceof WorldInitEvent) {
                FabricInjectBukkit.addEnumEnvironment(); // Banner
            }
        });
    }
}
