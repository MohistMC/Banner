package com.mohistmc.banner.mixin.core.world.level.chunk.storage;

import com.mohistmc.banner.injection.world.level.chunk.InjectionRegionFileStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(RegionFileStorage.class)
public abstract class MixinRegionFileStorage implements InjectionRegionFileStorage {

    // @formatter:off
    @Shadow protected abstract RegionFile getRegionFile(ChunkPos pos) throws IOException;
    // @formatter:on

    private RegionFile loadFile(ChunkPos pos, boolean existsOnly) throws IOException {
        this.banner$existOnly = existsOnly;
        return getRegionFile(pos);
    }

    private transient boolean banner$existOnly;

    @Inject(method = "getRegionFile", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "NEW", target = "net/minecraft/world/level/chunk/storage/RegionFile"))
    private void banner$retIfSearch(ChunkPos pos, CallbackInfoReturnable<RegionFile> cir, long l, RegionFile rf, Path path) {
        if (banner$existOnly && !Files.exists(path)) cir.setReturnValue(null);
    }

    @Inject(method = "read", at = @At("HEAD"))
    private void banner$read(ChunkPos pos, CallbackInfoReturnable<CompoundTag> cir) {
        this.banner$existOnly = true;
    }

    @Inject(method = "read", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/RegionFile;getChunkDataInputStream(Lnet/minecraft/world/level/ChunkPos;)Ljava/io/DataInputStream;"))
    private void banner$retIfNotFound(ChunkPos pos, CallbackInfoReturnable<CompoundTag> cir, RegionFile rf) {
        if (rf == null) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "write", at = @At("HEAD"))
    private void banner$write(ChunkPos pos, CompoundTag compound, CallbackInfo ci) {
        this.banner$existOnly = false;
    }

    @Inject(method = "scanChunk", at = @At("HEAD"))
    private void banner$scan(CallbackInfo ci) {
        this.banner$existOnly = true;
    }

    @Inject(method = "scanChunk", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/RegionFile;getChunkDataInputStream(Lnet/minecraft/world/level/ChunkPos;)Ljava/io/DataInputStream;"))
    private void banner$retIfNotFound(ChunkPos p_196957_, StreamTagVisitor p_196958_, CallbackInfo ci, RegionFile rf) {
        if (rf == null) {
            ci.cancel();
        }
    }

    @Override
    public boolean chunkExists(ChunkPos pos) throws IOException {
        RegionFile regionFile = loadFile(pos, true);
        return regionFile != null && regionFile.hasChunk(pos);
    }

}
