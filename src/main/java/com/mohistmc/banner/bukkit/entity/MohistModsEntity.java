package com.mohistmc.banner.bukkit.entity;

import com.mohistmc.banner.api.EntityAPI;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;

public class MohistModsEntity extends CraftEntity {

    public String entityName;

    public MohistModsEntity(CraftServer server, net.minecraft.world.entity.Entity entity) {
        super(server, entity);
        this.entityName = EntityAPI.entityName(entity);
    }

    @Override
    public net.minecraft.world.entity.Entity getHandle() {
        return this.entity;
    }

    @Override
    public String toString() {
        return "MohistModsEntity{" + entityName + '}';
    }
}
