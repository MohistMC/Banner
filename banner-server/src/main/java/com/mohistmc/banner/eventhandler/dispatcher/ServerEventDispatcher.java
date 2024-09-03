package com.mohistmc.banner.eventhandler.dispatcher;

import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServerLinks;
import org.bukkit.event.player.PlayerLinksSendEvent;

public class ServerEventDispatcher {

    public static void dispatchServer() {
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            var links = server.serverLinks();
            var wrapper = new CraftServerLinks(links);
            var event = new PlayerLinksSendEvent(handler.bridge$player().getBukkitEntity(), wrapper);
            Bukkit.getPluginManager().callEvent(event);
            server.setServerLinks(wrapper.getServerLinks());
        });
    }
}
