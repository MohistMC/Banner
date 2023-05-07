package com.mohistmc.banner.eventhandler.dispatcher;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerEventDispatcher {

    public static void dispatchServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Bukkit.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
        });
    }
}
