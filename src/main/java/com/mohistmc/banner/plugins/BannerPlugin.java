package com.mohistmc.banner.plugins;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class BannerPlugin {

    public static Logger LOGGER = LogManager.getLogger("BannerPlugin");

    public static void init() {
        EntityClear.start();
    }

    public static void registerListener(Event event) {
        if (event instanceof PrepareAnvilEvent prepareAnvilEvent) {
            EnchantmentFix.anvilListener(prepareAnvilEvent);
        }
    }
}
