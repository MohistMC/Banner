package com.mohistmc.banner.injection.world.level.storage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

public interface InjectionDerivedLevelData {

    default void setDimType(ResourceKey<LevelStem> typeKey) {
        throw new IllegalStateException("Not implemented");
    }
}
