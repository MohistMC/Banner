package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.npc.AbstractVillager;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftAbstractVillager;

public class BannerModAbstractVillager extends CraftAbstractVillager {

    public BannerModAbstractVillager(CraftServer server, AbstractVillager entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "BannerAbstractVillager{" + getType() + '}';
    }
}
