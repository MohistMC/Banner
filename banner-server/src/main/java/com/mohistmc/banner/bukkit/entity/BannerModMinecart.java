package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMinecart;

public class BannerModMinecart extends CraftMinecart {

    public BannerModMinecart(CraftServer server, AbstractMinecart entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModMinecart{" + getType() + '}';
    }
}
