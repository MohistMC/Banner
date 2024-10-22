package com.mohistmc.banner.injection.world.level.entity;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public interface InjectionPersistentEntitySectionManager {

    default List<Entity> getEntities(ChunkPos chunkCoordIntPair) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean isPending(long pair) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean storeChunkSections(long i, Consumer consumer, boolean callEvent) {
        throw new IllegalStateException("Not implemented");
    }

    default void close(boolean save) throws IOException {
        throw new IllegalStateException("Not implemented");
    }
}
