package com.mohistmc.banner.injection.world.level.chunk;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public interface InjectionLevelChunkSection {

    default void setBiome(int i, int j, int k, Holder<Biome> biome) {
        throw new IllegalStateException("Not implemented");
    }
}
