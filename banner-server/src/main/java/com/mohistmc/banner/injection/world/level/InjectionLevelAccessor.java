package com.mohistmc.banner.injection.world.level;

import org.bukkit.event.entity.CreatureSpawnEvent;

public interface InjectionLevelAccessor {
    default net.minecraft.server.level.ServerLevel getMinecraftWorld() {
        return null;
    }

    default void pushAddEntityReason(CreatureSpawnEvent.SpawnReason reason) {
    }
}
