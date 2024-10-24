package com.mohistmc.banner.plugin;

import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import com.mohistmc.banner.config.BannerConfig;
import net.minecraft.util.thread.NamedThreadFactory;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mgazul by MohistMC
 * @date 2023/7/25 23:56:03
 */
public class EntityClear {

    public static final ScheduledExecutorService ENTITYCLEAR = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("EntityClear"));

    public static void start() {
        ENTITYCLEAR.scheduleAtFixedRate(() -> {
            if (BukkitMethodHooks.getServer().hasStopped()) {
                return;
            }
            if (BannerConfig.clear_item) run();
        }, 1000 * 60 * 1, 1000 * BannerConfig.clear_item__time, TimeUnit.MILLISECONDS);
    }

    public static void run() {
        AtomicInteger size = new AtomicInteger(0);
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item) {
                    if (!BannerConfig.clear_item__whitelist.contains(item.getItemStack().getType().name())) {
                        entity.remove();
                        size.addAndGet(1);
                    }
                }
            }
        }
        if (!BannerConfig.clear_item__msg.equals("")) Bukkit.broadcastMessage(BannerConfig.clear_item__msg.replace("&", "§").replace("%size%", String.valueOf(size.getAndSet(0))));
    }
}