package com.mohistmc.banner.injection.world.level.spawner;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;

public interface InjectionSpawnState {

    default  boolean canSpawnForCategory(MobCategory enumcreaturetype, ChunkPos chunkcoordintpair, int limit) {
        throw new IllegalStateException("Not implemented");
    }
}
