package com.mohistmc.banner.bukkit.entity;

import com.mohistmc.banner.api.EntityAPI;
import net.minecraft.world.entity.raid.Raider;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftRaider;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;

public class MohistModsRaider extends CraftRaider {

    public String entityName;

    public MohistModsRaider(CraftServer server, Raider entity) {
        super(server, entity);
        this.entityName = EntityAPI.entityName(entity);
    }

    @Override
    public Raider getHandle() {
        return (Raider) this.entity;
    }


    @Override
    public EntityType getType() {
        return EntityAPI.entityType(entityName);
    }

    @Override
    public String toString() {
        return "MohistModsRaider{" + entityName + '}';
    }

    @Override
    public EntityCategory getCategory() {
        return EntityCategory.ILLAGER;
    }
}
