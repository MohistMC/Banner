package com.mohistmc.banner.injection.server.level;

import net.minecraft.world.level.chunk.LevelChunk;

public interface InjectionChunkHolder {

    default LevelChunk getFullChunkNow() {
        return null;
    }

    default LevelChunk getFullChunkNowUnchecked() {
        return null;
    }
}
