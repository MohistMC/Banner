package com.mohistmc.banner.injection.world.level.chunk;

import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

public interface InjectionChunkGenerator {

    default void addDecorations(WorldGenLevel region, ChunkAccess chunk, StructureManager structureManager) {
        throw new IllegalStateException("Not implemented");
    }

    default void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunkAccess, StructureManager structureFeatureManager, boolean vanilla) {
        throw new IllegalStateException("Not implemented");
    }
}
