package com.mohistmc.banner.injection.world.level;

import net.minecraft.world.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public interface InjectionLevelWriter {

    default boolean addFreshEntity(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        return false;
    }

    default boolean addEntity(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        return false;
    }

    default CreatureSpawnEvent.SpawnReason getAddEntityReason() {
        return CreatureSpawnEvent.SpawnReason.DEFAULT;
    }
}
