package com.mohistmc.banner.injection.world.level;

import net.minecraft.world.entity.Entity;

public interface InjectionServerLevelAccessor {

    default void addFreshEntityWithPassengers(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
    }
}
