package com.mohistmc.banner.plugin;

import com.mohistmc.banner.api.event.EntityJoinWorldEvent;
import com.mohistmc.banner.api.event.block.BlockDestroyEvent;
import com.mohistmc.banner.config.BannerConfig;
import com.mohistmc.banner.config.BannerWorldConfig;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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

    @EventHandler
    public static void onEntityBreakBlock(BlockDestroyEvent event) {
        var entity = event.getEntity();
        if (BannerConfig.banned_breakable_entities.contains(entity.getType().name())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onEntityExplode(EntityExplodeEvent event) {
        var entity = event.getEntity();
        if (BannerConfig.banned_breakable_entities.contains(entity.getType().name())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onTntExplode(TNTPrimeEvent event) {
        if (BannerConfig.banned_tnt) {
            event.setCancelled(true);
        }
    }
}
