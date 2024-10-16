package com.mohistmc.banner.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class BannerEventListener implements Listener {


    @EventHandler
    public static void onAnvilFix(PrepareAnvilEvent event) {
        EnchantmentFix.anvilListener(event);
    }
}
