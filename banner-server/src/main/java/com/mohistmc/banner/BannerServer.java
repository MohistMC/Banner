package com.mohistmc.banner;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;

public class BannerServer {

    @Deprecated
    public static RegistryAccess getDefaultRegistryAccess() {
        return CraftRegistry.getMinecraftRegistry();
    }

    public static MinecraftServer getServer() {
        return Bukkit.getServer() instanceof CraftServer ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }
}
