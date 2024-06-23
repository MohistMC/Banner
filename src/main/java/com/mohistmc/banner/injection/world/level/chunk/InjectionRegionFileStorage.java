package com.mohistmc.banner.injection.world.level.chunk;

import java.io.IOException;
import net.minecraft.world.level.ChunkPos;

public interface InjectionRegionFileStorage {

    default boolean chunkExists(ChunkPos pos) throws IOException {
        throw new IllegalStateException("Not implemented");
    }
}
