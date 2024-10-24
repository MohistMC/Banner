package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftThrowableProjectile;

public class BannerModThrowableProjectile extends CraftThrowableProjectile {

    public BannerModThrowableProjectile(CraftServer server, ThrowableItemProjectile entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModThrowableProjectile{" + getType() + '}';
    }
}
