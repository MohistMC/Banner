package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftAbstractWindCharge;

public class BannerModWindCharge extends CraftAbstractWindCharge {

    public BannerModWindCharge(CraftServer server, AbstractWindCharge entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModWindCharge{" + getType() + '}';
    }
}
