package com.mohistmc.banner.injection.world.level.levelgen;

import net.minecraft.world.level.biome.BiomeSource;

public interface InjectionFlatLevelSource {

    default void banner$setBiomeSource(BiomeSource biomeSource) {
        throw new IllegalStateException("Not implemented");
    }
}
