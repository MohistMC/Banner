package com.mohistmc.banner.injection.world.level.block.entity;

import net.minecraft.world.level.Level;

public interface InjectionCatalystListener {

    default void banner$setLevel(Level level) {
        throw new IllegalStateException("Not implemented");
    }
}
