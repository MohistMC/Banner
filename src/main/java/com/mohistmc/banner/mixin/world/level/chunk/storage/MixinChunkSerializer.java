package com.mohistmc.banner.mixin.world.level.chunk.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class MixinChunkSerializer {

    @Redirect(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;setLightCorrect(Z)V"))
    private static void banner$loadPersistent(ChunkAccess instance, boolean correct, ServerLevel level, PoiManager poiManager, ChunkPos pos, CompoundTag tag) {
        net.minecraft.nbt.Tag persistentBase = tag.get("ChunkBukkitValues");
        if (persistentBase instanceof CompoundTag) {
            ((CraftPersistentDataContainer) (instance).bridge$persistentDataContainer()).putAll((CompoundTag) persistentBase);
        }
        instance.setLightCorrect(correct);
    }

    @Inject(method = "method_39797", at = @At("HEAD"))
    private static void banner$timings(ListTag listTag, ServerLevel serverLevel, ListTag listTag2, LevelChunk levelChunk, CallbackInfo ci) {
        serverLevel.bridge$timings().syncChunkLoadEntitiesTimer.startTiming(); // Spigot
    }

    @Inject(method = "method_39797", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addLegacyChunkEntities(Ljava/util/stream/Stream;)V",
            shift = At.Shift.AFTER))
    private static void banner$timings0(ListTag listTag, ServerLevel serverLevel, ListTag listTag2, LevelChunk levelChunk, CallbackInfo ci) {
        serverLevel.bridge$timings().syncChunkLoadEntitiesTimer.stopTiming(); // Spigot
        serverLevel.bridge$timings().syncChunkLoadTileEntitiesTimer.startTiming(); // Spigot
    }

    @Inject(method = "method_39797", at = @At("TAIL"))
    private static void banner$timings1(ListTag listTag, ServerLevel serverLevel, ListTag listTag2, LevelChunk levelChunk, CallbackInfo ci) {
        serverLevel.bridge$timings().syncChunkLoadTileEntitiesTimer.stopTiming(); // Spigot
    }

    @Inject(method = "write", at = @At("RETURN"))
    private static void banner$savePersistent(ServerLevel level, ChunkAccess chunkAccess, CallbackInfoReturnable<CompoundTag> cir) {
        var container = (CraftPersistentDataContainer) (chunkAccess).bridge$persistentDataContainer();
        if (!container.isEmpty()) {
            cir.getReturnValue().put("ChunkBukkitValues", container.toTagCompound());
        }
    }
}
