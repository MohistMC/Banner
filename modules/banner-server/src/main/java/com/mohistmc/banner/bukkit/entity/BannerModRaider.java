package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.raid.Raider;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftRaider;

public class BannerModRaider extends CraftRaider {

    public BannerModRaider(CraftServer server, Raider entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerModRaider{" + getType() + '}';
    }
}
