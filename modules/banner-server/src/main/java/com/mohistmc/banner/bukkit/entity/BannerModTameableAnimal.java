package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftTameableAnimal;

public class BannerModTameableAnimal extends CraftTameableAnimal {

    public BannerModTameableAnimal(CraftServer server, TamableAnimal entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModTameableAnimal{" + getType() + '}';
    }
}
