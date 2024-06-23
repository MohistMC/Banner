package com.mohistmc.banner.injection.world.level.border;

import net.minecraft.world.level.Level;

public interface InjectionWorldBorder {

    default Level bridge$world() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setWorld(Level world) {
        throw new IllegalStateException("Not implemented");
    }
}
