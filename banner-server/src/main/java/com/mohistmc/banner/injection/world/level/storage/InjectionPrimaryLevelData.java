package com.mohistmc.banner.injection.world.level.storage;

import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.LevelStem;

public interface InjectionPrimaryLevelData {

    default Registry<LevelStem> bridge$customDimensions() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCustomDimensions(Registry<LevelStem> customDimensions) {
        throw new IllegalStateException("Not implemented");
    }

    default void checkName(String name) {
        throw new IllegalStateException("Not implemented");
    }

    default void setWorld(ServerLevel world) {
        throw new IllegalStateException("Not implemented");
    }
}
