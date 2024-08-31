package com.mohistmc.banner.injection.world.level.block;

import net.minecraft.world.level.Level;

public interface InjectionSculkSpreader {

    default void banner$setLevel(Level level) {
        throw new IllegalStateException("Not implemented");
    }
}
