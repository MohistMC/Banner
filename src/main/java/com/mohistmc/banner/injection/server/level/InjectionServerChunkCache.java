package com.mohistmc.banner.injection.server.level;

import java.io.IOException;

public interface InjectionServerChunkCache {

    default boolean isChunkLoaded(int chunkX, int chunkZ) {
        return false;
    }

    default void close(boolean save) throws IOException {
    }

    default void purgeUnload() {
    }
}
