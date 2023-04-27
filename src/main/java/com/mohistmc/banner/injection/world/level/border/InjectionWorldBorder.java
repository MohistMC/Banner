package com.mohistmc.banner.injection.world.level.border;

import net.minecraft.server.level.ServerLevel;

public interface InjectionWorldBorder {

    default ServerLevel bridge$world() {
        return null;
    }

    default void banner$setWorld(ServerLevel world) {
    }
}
