package com.mohistmc.banner.injection.world.level.storage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.IOException;

public interface InjectionLevelStorageSource {

    default LevelStorageSource.LevelStorageAccess createAccess(String s, ResourceKey<LevelStem> dimensionType) {
        return null;
    }
}
