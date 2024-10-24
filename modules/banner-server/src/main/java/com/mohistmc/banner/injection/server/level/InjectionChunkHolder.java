package com.mohistmc.banner.injection.server.level;

import net.minecraft.world.level.chunk.LevelChunk;

public interface InjectionChunkHolder {

    default LevelChunk getFullChunkNow() {
        throw new IllegalStateException("Not implemented");
    }

    default LevelChunk getFullChunkNowUnchecked() {
        throw new IllegalStateException("Not implemented");
    }
}
