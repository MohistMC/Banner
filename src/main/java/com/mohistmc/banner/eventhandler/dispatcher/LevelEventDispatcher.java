package com.mohistmc.banner.eventhandler.dispatcher;

import com.mohistmc.banner.fabric.FabricHookBukkitEvent;
import com.mohistmc.banner.fabric.FabricInjectBukkit;
import org.bukkit.event.world.WorldInitEvent;

public class LevelEventDispatcher {

    public static void dispatchLevel() {
        FabricHookBukkitEvent.EVENT.register(bukkitEvent -> {
            if (bukkitEvent instanceof WorldInitEvent) {
                FabricInjectBukkit.addEnumEnvironment(); // Banner
            }
        });
    }
}
