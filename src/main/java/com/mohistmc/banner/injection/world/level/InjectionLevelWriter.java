package com.mohistmc.banner.injection.world.level;

import net.minecraft.world.entity.Entity;

public interface InjectionLevelWriter {

    default boolean addFreshEntity(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        return false;
    }
}
