package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;

public class BannerModLivingEntity extends CraftLivingEntity {

    public BannerModLivingEntity(CraftServer server, LivingEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModLivingEntity{" + getType() + '}';
    }
}
