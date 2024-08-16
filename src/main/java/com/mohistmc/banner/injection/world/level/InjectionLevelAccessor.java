package com.mohistmc.banner.injection.world.level;

import org.bukkit.event.entity.CreatureSpawnEvent;

public interface InjectionLevelAccessor {
    default net.minecraft.server.level.ServerLevel getMinecraftWorld() {
        throw new IllegalStateException("Not implemented");
    }

    default void pushAddEntityReason(CreatureSpawnEvent.SpawnReason reason) {
    }
}
