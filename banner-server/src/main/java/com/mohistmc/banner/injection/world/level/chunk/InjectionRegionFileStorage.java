package com.mohistmc.banner.injection.world.level.chunk;

import net.minecraft.world.level.ChunkPos;

import java.io.IOException;

public interface InjectionRegionFileStorage {

    default boolean chunkExists(ChunkPos pos) throws IOException {
        throw new IllegalStateException("Not implemented");
    }
}
