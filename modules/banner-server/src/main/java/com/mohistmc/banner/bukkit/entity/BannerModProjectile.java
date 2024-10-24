package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.projectile.Projectile;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftProjectile;

public class BannerModProjectile extends CraftProjectile {

    public BannerModProjectile(CraftServer server, Projectile entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModProjectile{" + getType() + '}';
    }
}
