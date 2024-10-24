package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.animal.Animal;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftAnimals;

public class BannerModAnimals extends CraftAnimals {

    public BannerModAnimals(CraftServer server, Animal entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModAnimals{" + getType() + '}';
    }
}
