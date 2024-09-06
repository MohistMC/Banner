package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMinecartContainer;

public class BannerModMinecartContainer extends CraftMinecartContainer {

    public BannerModMinecartContainer(CraftServer server, AbstractMinecart entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModMinecartContainer{" + getType() + '}';
    }
}
