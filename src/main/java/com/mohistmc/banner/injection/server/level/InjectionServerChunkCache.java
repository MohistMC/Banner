package com.mohistmc.banner.injection.server.level;

import java.io.IOException;
import net.minecraft.world.level.chunk.LevelChunk;

public interface InjectionServerChunkCache {

    default LevelChunk getChunkUnchecked(int chunkX, int chunkZ) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean isChunkLoaded(int chunkX, int chunkZ) {
        throw new IllegalStateException("Not implemented");
    }

    default void close(boolean save) throws IOException {
        throw new IllegalStateException("Not implemented");
    }

    default void purgeUnload() {
        throw new IllegalStateException("Not implemented");
    }
}
