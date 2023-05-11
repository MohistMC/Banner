package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionChunkHolder;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;

// TODO fix inject method
@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder implements InjectionChunkHolder {


    // @formatter:off
    @Shadow public int oldTicketLevel;
    @Shadow public abstract CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getFutureIfPresentUnchecked(ChunkStatus p_219301_1_);
    @Shadow @Final ChunkPos pos;
    @Shadow @Final private ShortSet[] changedBlocksPerSection;


    @Override
    public LevelChunk getFullChunkNow() {
        // Banner TODO - wait for spigot
        /**
        if (!ChunkHolder.getFullChunkStatus(this.oldTicketLevel).isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
            return null; // note: using oldTicketLevel for isLoaded checks
        }*/
        return this.getFullChunkNowUnchecked();
    }

    @Override
    public LevelChunk getFullChunkNowUnchecked() {
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> statusFuture = this.getFutureIfPresentUnchecked(ChunkStatus.FULL);
        Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = statusFuture.getNow(null);
        return (either == null) ? null : (LevelChunk) either.left().orElse(null);
    }

    @Inject(method = "blockChanged", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "FIELD", ordinal = 0, target = "Lnet/minecraft/server/level/ChunkHolder;changedBlocksPerSection:[Lit/unimi/dsi/fastutil/shorts/ShortSet;"))
    private void arclight$outOfBound(BlockPos pos, CallbackInfo ci, LevelChunk chunk, int i) {
        if (i < 0 || i >= this.changedBlocksPerSection.length) {
            ci.cancel();
        }
    }
}
