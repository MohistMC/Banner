package com.mohistmc.banner.injection.world.level;

import net.minecraft.world.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public interface InjectionServerLevelAccessor {

    default void addFreshEntityWithPassengers(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
    }

    default CreatureSpawnEvent.SpawnReason bridge$getAddEntityReason() {
        return null;
    }
}
