package com.mohistmc.banner.plugin;

import com.mohistmc.banner.api.event.EntityJoinWorldEvent;
import com.mohistmc.banner.config.BannerConfig;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class BannerEventListener implements Listener {

    @EventHandler
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        var entity = event.getEntity();
        if (BannerConfig.banned_entities.contains(entity.getType().name())) {
            entity.remove();
            ((CraftEntity) entity).getHandle().pushRemoveCause(EntityRemoveEvent.Cause.PLUGIN);
        }
    }

    @EventHandler
    public static void onAnvilFix(PrepareAnvilEvent event) {
        EnchantmentFix.anvilListener(event);
    }
}
