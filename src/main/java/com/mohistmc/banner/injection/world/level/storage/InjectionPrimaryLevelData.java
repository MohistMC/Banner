package com.mohistmc.banner.injection.world.level.storage;

import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.LevelStem;

public interface InjectionPrimaryLevelData {

    default Registry<LevelStem> bridge$customDimensions() {
        return null;
    }

    default void banner$setCustomDimensions(Registry<LevelStem> customDimensions) {
    }

    default void checkName(String name) {
    }

    default void setWorld(ServerLevel world) {
    }
}
