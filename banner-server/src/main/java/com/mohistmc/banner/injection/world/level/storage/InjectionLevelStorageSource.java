package com.mohistmc.banner.injection.world.level.storage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.ContentValidationException;

import java.io.IOException;

public interface InjectionLevelStorageSource {

    default LevelStorageSource.LevelStorageAccess validateAndCreateAccess(String string, ResourceKey<LevelStem> dimensionType) throws IOException, ContentValidationException { // CraftBukkit
        throw new IllegalStateException("Not implemented");
    }
}
