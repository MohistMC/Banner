package com.mohistmc.banner.mixin.world.level.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public abstract class MixinNaturalSpawner {

    // @formatter:off
    @Shadow @Final private static MobCategory[] SPAWNING_CATEGORIES;
    // @formatter:on

    @Inject(method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private static void banner$naturalSpawn(MobCategory category, ServerLevel level, ChunkAccess chunk, BlockPos pos, NaturalSpawner.SpawnPredicate filter, NaturalSpawner.AfterSpawnCallback callback, CallbackInfo ci) {
         level.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.NATURAL);
    }

    @Redirect(method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;run(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/level/chunk/ChunkAccess;)V"))
    private static void banner$skipRun(NaturalSpawner.AfterSpawnCallback afterSpawnCallback, Mob mob, ChunkAccess chunkAccess) {
        if (!mob.isRemoved()) {
            afterSpawnCallback.run(mob, chunkAccess);
        }
    }

    @Inject(method = "spawnMobsForChunkGeneration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerLevelAccessor;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private static void banner$worldGenSpawn(ServerLevelAccessor levelAccessor, Holder<Biome> biome, ChunkPos chunkPos, RandomSource random, CallbackInfo ci) {
        levelAccessor.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.CHUNK_GEN);
    }
}
