package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.Mob;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMob;

public class MohistModsMob extends CraftMob {

    public MohistModsMob(CraftServer server, Mob entity) {
        super(server, entity);
    }
}
