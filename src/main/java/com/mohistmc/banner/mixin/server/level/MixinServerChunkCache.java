package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionServerChunkCache;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.storage.LevelData;
import org.bukkit.entity.SpawnCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.BooleanSupplier;

@Mixin(ServerChunkCache.class)
public abstract class MixinServerChunkCache implements InjectionServerChunkCache {

    // @formatter:off
    @Shadow public abstract void save(boolean flush);
    @Shadow @Final ThreadedLevelLightEngine lightEngine;
    @Shadow @Final public ChunkMap chunkMap;
    @Shadow @Final public ServerLevel level;
    @Shadow @Final private DistanceManager distanceManager;
    @Shadow protected abstract void clearCache();
    @Shadow @Nullable protected abstract ChunkHolder getVisibleChunkIfPresent(long chunkPosIn);

    @Shadow abstract boolean runDistanceManagerUpdates();

    @Override
    public boolean isChunkLoaded(final int chunkX, final int chunkZ) {
        ChunkHolder chunk =  this.chunkMap.getUpdatingChunkIfPresent(ChunkPos.asLong(chunkX, chunkZ));
        return chunk != null &&  chunk.getFullChunkNow() != null;
    }

    @ModifyVariable(method = "getChunkFutureMainThread", index = 4, at = @At("HEAD"), argsOnly = true)
    private boolean banner$skipIfUnloading(boolean flag, int chunkX, int chunkZ) {
        if (flag) {
            ChunkHolder chunkholder = this.getVisibleChunkIfPresent(ChunkPos.asLong(chunkX, chunkZ));
            if (chunkholder != null) {
                ChunkHolder.FullChunkStatus chunkStatus = ChunkHolder.getFullChunkStatus(chunkholder.oldTicketLevel);
                ChunkHolder.FullChunkStatus currentStatus = ChunkHolder.getFullChunkStatus(chunkholder.getTicketLevel());
                return !chunkStatus.isOrAfter(ChunkHolder.FullChunkStatus.BORDER) || currentStatus.isOrAfter(ChunkHolder.FullChunkStatus.BORDER);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean banner$noPlayer(GameRules gameRules, GameRules.Key<GameRules.BooleanValue> key) {
        return gameRules.getBoolean(key) && !this.level.players().isEmpty();
    }

    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelData;getGameTime()J"))
    private long banner$ticksPer(LevelData worldInfo) {
        long gameTime = worldInfo.getGameTime();
        long ticksPer = this.level.bridge$ticksPerSpawnCategory().getLong(SpawnCategory.ANIMAL);
        return (ticksPer != 0L && gameTime % ticksPer == 0) ? 0 : 1;
    }

    @Inject(method = "getChunk", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;incrementCounter(Ljava/lang/String;)V", ordinal = 1))
    private void banner$timingsStart(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load, CallbackInfoReturnable<ChunkAccess> cir) {
        level.bridge$timings().syncChunkLoadTimer.startTiming(); // Spigot
    }

    @Inject(method = "getChunk", at = @At(value = "INVOKE",
            target = "Lcom/mojang/datafixers/util/Either;map(Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;"))
    private void banner$timingsStop(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load, CallbackInfoReturnable<ChunkAccess> cir) {
        level.bridge$timings().syncChunkLoadTimer.stopTiming(); // Spigot
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/DistanceManager;purgeStaleTickets()V"))
    private void banner$timings(BooleanSupplier hasTimeLeft, boolean tickChunks, CallbackInfo ci) {
        this.level.bridge$timings().doChunkMap.startTiming(); // Spigot
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
            ordinal = 0))
    private void banner$timings0(BooleanSupplier hasTimeLeft, boolean tickChunks, CallbackInfo ci) {
        this.level.bridge$timings().doChunkMap.stopTiming(); // Spigot
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
            ordinal = 1))
    private void banner$timings1(BooleanSupplier hasTimeLeft, boolean tickChunks, CallbackInfo ci) {
        this.level.bridge$timings().doChunkUnload.startTiming(); // Spigot
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"))
    private void banner$timings2(BooleanSupplier hasTimeLeft, boolean tickChunks, CallbackInfo ci) {
        this.level.bridge$timings().doChunkUnload.stopTiming(); // Spigot
    }

    @Inject(method = "tickChunks", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;tickChunk(Lnet/minecraft/world/level/chunk/LevelChunk;I)V"))
    private void banner$timings3(CallbackInfo ci) {
        this.level.bridge$timings().doTickTiles.startTiming(); // Spigot
    }

    @Inject(method = "tickChunks", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;tickChunk(Lnet/minecraft/world/level/chunk/LevelChunk;I)V",
            shift = At.Shift.AFTER))
    private void banner$timings4(CallbackInfo ci) {
        this.level.bridge$timings().doTickTiles.stopTiming(); // Spigot
    }

    @Inject(method = "tickChunks", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ChunkMap;tick()V",
            ordinal = 1))
    private void banner$timings5(CallbackInfo ci) {
        this.level.bridge$timings().tracker.startTiming(); // Spigot
    }

    @Inject(method = "tickChunks", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ChunkMap;tick()V",
            shift = At.Shift.AFTER,
            ordinal = 1))
    private void banner$timings6(CallbackInfo ci) {
        this.level.bridge$timings().tracker.stopTiming(); // Spigot
    }

    @Override
    public void close(boolean save) throws IOException {
        if (save) {
            this.save(true);
        }
        this.lightEngine.close();
        this.chunkMap.close();
    }

    @Override
    public void purgeUnload() {
        this.level.getProfiler().push("purge");
        this.distanceManager.purgeStaleTickets();
        this.runDistanceManagerUpdates();
        this.level.getProfiler().popPush("unload");
        this.chunkMap.tick(() -> true);
        this.level.getProfiler().pop();
        this.clearCache();
    }

    @Redirect(method = "chunkAbsent", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;getTicketLevel()I"))
    public int banner$useOldTicketLevel(ChunkHolder chunkHolder) {
        return chunkHolder.oldTicketLevel;
    }
}
