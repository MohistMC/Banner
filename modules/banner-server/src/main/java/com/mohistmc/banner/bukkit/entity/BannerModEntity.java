package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;

public class BannerModEntity extends CraftEntity {

    public BannerModEntity(CraftServer server, Entity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModEntity{" + getType() + '}';
    }
}
