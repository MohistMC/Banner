package com.mohistmc.banner.injection.world.level;

import net.minecraft.world.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public interface InjectionLevelWriter {

    default boolean addFreshEntity(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean addEntity(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        throw new IllegalStateException("Not implemented");
    }

    default CreatureSpawnEvent.SpawnReason getAddEntityReason() {
        return CreatureSpawnEvent.SpawnReason.DEFAULT;
    }
}
