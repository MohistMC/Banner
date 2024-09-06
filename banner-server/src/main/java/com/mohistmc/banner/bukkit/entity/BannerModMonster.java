package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.monster.Monster;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMonster;

public class BannerModMonster extends CraftMonster {

    public BannerModMonster(CraftServer server, Monster entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModMonster{" + getType() + '}';
    }
}
