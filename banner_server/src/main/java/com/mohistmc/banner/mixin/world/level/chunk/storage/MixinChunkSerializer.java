package com.mohistmc.banner.mixin.world.level.chunk.storage;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkSerializer.class)
public class MixinChunkSerializer {

    @Inject(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;setLightCorrect(Z)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void banner$loadPersistent(ServerLevel serverLevel, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos, CompoundTag compoundTag, CallbackInfoReturnable<ProtoChunk> cir, ChunkPos chunkPos2, UpgradeData upgradeData, boolean bl, ListTag listTag, int i, LevelChunkSection[] levelChunkSections, boolean bl2, ChunkSource chunkSource, LevelLightEngine levelLightEngine, Registry registry, Codec codec, boolean bl3, long m, ChunkType chunkType, BlendingData blendingData, ChunkAccess chunkAccess) {
        net.minecraft.nbt.Tag persistentBase = compoundTag.get("ChunkBukkitValues");
        if (persistentBase instanceof CompoundTag) {
            ((CraftPersistentDataContainer) (chunkAccess).bridge$persistentDataContainer()).putAll((CompoundTag) persistentBase);
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private static void banner$savePersistent(ServerLevel level, ChunkAccess chunkAccess, CallbackInfoReturnable<CompoundTag> cir) {
        var container = (CraftPersistentDataContainer) (chunkAccess).bridge$persistentDataContainer();
        if (!container.isEmpty()) {
            cir.getReturnValue().put("ChunkBukkitValues", container.toTagCompound());
        }
    }
}
