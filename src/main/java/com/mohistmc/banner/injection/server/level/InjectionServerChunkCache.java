package com.mohistmc.banner.injection.server.level;

import java.io.IOException;
import net.minecraft.world.level.chunk.LevelChunk;

public interface InjectionServerChunkCache {

    default LevelChunk getChunkUnchecked(int chunkX, int chunkZ) {
        return null;
    }

    default boolean isChunkLoaded(int chunkX, int chunkZ) {
        return false;
    }

    default void close(boolean save) throws IOException {
    }

    default void purgeUnload() {
    }
}
