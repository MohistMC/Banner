package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftVehicle;

public class BannerModVehicle extends CraftVehicle {

    public BannerModVehicle(CraftServer server, Entity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModModVehicle{" + getType() + '}';
    }
}
