package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.npc.Villager;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftVillager;

public class BannerModVillager extends CraftVillager {

    public BannerModVillager(CraftServer server, Villager entity) {
        super(server, entity);
    }

    @Override
    public Profession getProfession() {
        return super.getProfession() != null ? super.getProfession() : Profession.NONE;
    }

    @Override
    public String toString() {
        return "BannerModVillager{" + getType() + '}';
    }
}
