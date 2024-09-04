package com.mohistmc.banner.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BannerPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Banner Plugin is enabled.");
        EntityClear.start();
        Bukkit.getPluginManager().registerEvents(new BannerEventListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Banner Plugin is disabled!");
    }
}
