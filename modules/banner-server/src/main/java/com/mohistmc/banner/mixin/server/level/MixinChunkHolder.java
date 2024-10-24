package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionChunkHolder;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO fix inject method
@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder extends GenerationChunkHolder implements InjectionChunkHolder {


    // @formatter:off
    @Shadow public int oldTicketLevel;
    @Shadow @Final private ShortSet[] changedBlocksPerSection;
    @Shadow private int ticketLevel;
    // @formatter:on

    public MixinChunkHolder(ChunkPos chunkPos) {
        super(chunkPos);
    }

    @Shadow public abstract CompletableFuture<ChunkResult<LevelChunk>> getFullChunkFuture();

    @Shadow @Final private LevelHeightAccessor levelHeightAccessor;

    @Override
    public LevelChunk getFullChunkNow() {
        // Note: We use the oldTicketLevel for isLoaded checks.
        if (!ChunkLevel.fullStatus(this.oldTicketLevel).isOrAfter(FullChunkStatus.FULL)) return null;
        return this.getFullChunkNowUnchecked();
    }

    @Override
    public LevelChunk getFullChunkNowUnchecked() {
        return (LevelChunk) this.getChunkIfPresentUnchecked(ChunkStatus.FULL);
    }

    @Inject(method = "blockChanged", cancellable = true,
            at = @At(value = "FIELD", ordinal = 0, target = "Lnet/minecraft/server/level/ChunkHolder;changedBlocksPerSection:[Lit/unimi/dsi/fastutil/shorts/ShortSet;"))
    private void banner$outOfBound(BlockPos pos, CallbackInfo ci) {
        int i = this.levelHeightAccessor.getSectionIndex(pos.getY());
        if (i < 0 || i >= this.changedBlocksPerSection.length) {
            ci.cancel();
        }
    }

    @Inject(method = "updateFutures", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ChunkLevel;fullStatus(I)Lnet/minecraft/server/level/FullChunkStatus;",
            ordinal = 1))
    private void banner$chunkEvent0(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
        // CraftBukkit start
        // ChunkUnloadEvent: Called before the chunk is unloaded: isChunkLoaded is still true and chunk can still be modified by plugins.
        if (ChunkLevel.fullStatus(this.oldTicketLevel).isOrAfter(FullChunkStatus.FULL) && !ChunkLevel.fullStatus(this.ticketLevel).isOrAfter(FullChunkStatus.FULL)) {
            this.getFullChunkFuture().thenAccept((either) -> {
                LevelChunk chunk = (LevelChunk)either.orElse(null);
                if (chunk != null) {
                    chunkMap.bridge$callbackExecutor().execute(() -> {
                        // Minecraft will apply the chunks tick lists to the world once the chunk got loaded, and then store the tick
                        // lists again inside the chunk once the chunk becomes inaccessible and set the chunk's needsSaving flag.
                        // These actions may however happen deferred, so we manually set the needsSaving flag already here.
                        chunk.setUnsaved(true);
                        chunk.unloadCallback();
                    });
                }
            }).exceptionally((throwable) -> {
                // ensure exceptions are printed, by default this is not the case
                MinecraftServer.LOGGER.error("Failed to schedule unload callback for chunk " + this.pos, throwable);
                return null;
            });

            // Run callback right away if the future was already done
            chunkMap.bridge$callbackExecutor().run();
        }
        // CraftBukkit end
    }

    @Inject(method = "updateFutures", at = @At("TAIL"))
    private void banner$chunkEvent1(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
        // CraftBukkit start
        // ChunkLoadEvent: Called after the chunk is loaded: isChunkLoaded returns true and chunk is ready to be modified by plugins.
        if (!ChunkLevel.fullStatus(this.oldTicketLevel).isOrAfter(FullChunkStatus.FULL) && ChunkLevel.fullStatus(this.ticketLevel).isOrAfter(FullChunkStatus.FULL)) {
            this.getFullChunkFuture().thenAccept((either) -> {
                LevelChunk chunk = (LevelChunk)either.orElse(null);
                if (chunk != null) {
                    chunkMap.bridge$callbackExecutor().execute(() -> {
                        chunk.loadCallback();
                    });
                }
            }).exceptionally((throwable) -> {
                // ensure exceptions are printed, by default this is not the case
                MinecraftServer.LOGGER.error("Failed to schedule load callback for chunk " + this.pos, throwable);
                return null;
            });

            // Run callback right away if the future was already done
            chunkMap.bridge$callbackExecutor().run();
        }
        // CraftBukkit end
    }
}
