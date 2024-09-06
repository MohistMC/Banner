package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.animal.AbstractGolem;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftGolem;

public class BannerModGolem extends CraftGolem {

    public BannerModGolem(CraftServer server, AbstractGolem entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModGolem{" + getType() + '}';
    }
}
