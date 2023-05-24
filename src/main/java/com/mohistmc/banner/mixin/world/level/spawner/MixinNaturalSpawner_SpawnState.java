package com.mohistmc.banner.mixin.world.level.spawner;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NaturalSpawner.SpawnState.class)
public class MixinNaturalSpawner_SpawnState {

    @Shadow @Final private int spawnableChunkCount;

    @Shadow @Final private Object2IntOpenHashMap<MobCategory> mobCategoryCounts;

    @Shadow @Final private LocalMobCapCalculator localMobCapCalculator;

    // CraftBukkit start
    boolean canSpawnForCategory(MobCategory enumcreaturetype, ChunkPos chunkcoordintpair, int limit) {
        int i = limit * this.spawnableChunkCount / NaturalSpawner.MAGIC_NUMBER;
        // CraftBukkit end

        return this.mobCategoryCounts.getInt(enumcreaturetype) >= i ? false : this.localMobCapCalculator.canSpawn(enumcreaturetype, chunkcoordintpair);
    }
}
