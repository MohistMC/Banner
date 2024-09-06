package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.Mob;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMob;

public class BannerModMob extends CraftMob {

    public BannerModMob(CraftServer server, Mob entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModMob{" + getType() + '}';
    }
}
