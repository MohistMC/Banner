package com.mohistmc.banner.mixin.world.level.chunk.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Mixin(ChunkStorage.class)
public abstract class MixinChunkStorage {

    private AtomicReference<Boolean> stopBelowZero = new AtomicReference<>();

    @Redirect(method = "getLegacyStructureHandler", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/LegacyStructureDataHandler;getLegacyStructureHandler(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/storage/DimensionDataStorage;)Lnet/minecraft/world/level/levelgen/structure/LegacyStructureDataHandler;"))
    private LegacyStructureDataHandler banner$legacyData(ResourceKey<Level> level, DimensionDataStorage storage) {
        return legacyDataOf(level, storage);
    }

    /**
     * From {@link LegacyStructureDataHandler#getLegacyStructureHandler(ResourceKey, DimensionDataStorage)}
     */
    private static LegacyStructureDataHandler legacyDataOf(ResourceKey<?> typeKey, @Nullable DimensionDataStorage dataManager) {
        if (typeKey == LevelStem.OVERWORLD || typeKey == Level.OVERWORLD) {
            return new LegacyStructureDataHandler(dataManager, ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"), ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument"));
        } else if (typeKey == LevelStem.NETHER || typeKey == Level.NETHER) {
            List<String> list1 = ImmutableList.of("Fortress");
            return new LegacyStructureDataHandler(dataManager, list1, list1);
        } else if (typeKey == LevelStem.END || typeKey == Level.END) {
            List<String> list = ImmutableList.of("EndCity");
            return new LegacyStructureDataHandler(dataManager, list, list);
        } else {
            throw new RuntimeException(String.format("Unknown dimension type : %s", typeKey));
        }
    }

    @Inject(method = "upgradeChunkTag",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/storage/ChunkStorage;injectDatafixingContext(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/resources/ResourceKey;Ljava/util/Optional;)V",
            shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$belowZeroGeneration(ResourceKey<Level> levelKey, Supplier<DimensionDataStorage> storage, CompoundTag chunkData, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> chunkGeneratorKey, CallbackInfoReturnable<CompoundTag> cir, int i) {
        // Spigot start - SPIGOT-6806: Quick and dirty way to prevent below zero generation in old chunks, by setting the status to heightmap instead of empty
        stopBelowZero.set(false);
        boolean belowZeroGenerationInExistingChunks = (storage != null) ? ((ServerLevel) storage).bridge$spigotConfig().belowZeroGenerationInExistingChunks : org.spigotmc.SpigotConfig.belowZeroGenerationInExistingChunks;
        if (i <= 2730 && !belowZeroGenerationInExistingChunks) {
            stopBelowZero.set("full".equals(chunkData.getCompound("Level").getString("Status")));
        }
        // Spigot end
    }

    @Inject(method = "upgradeChunkTag",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;remove(Ljava/lang/String;)V",
            shift = At.Shift.BEFORE))
    private void banner$putChunkTag(ResourceKey<Level> levelKey, Supplier<DimensionDataStorage> storage, CompoundTag chunkData, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> chunkGeneratorKey, CallbackInfoReturnable<CompoundTag> cir) {
        // Spigot start
        if (stopBelowZero.get()) {
            chunkData.putString("Status",  net.minecraft.core.registries.BuiltInRegistries.CHUNK_STATUS.getKey(ChunkStatus.SPAWN).toString());
        }
        // Spigot end
    }
}
