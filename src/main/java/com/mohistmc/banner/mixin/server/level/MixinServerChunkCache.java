package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionServerChunkCache;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelData;
import org.bukkit.entity.SpawnCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkCache.class)
public abstract class MixinServerChunkCache implements InjectionServerChunkCache {

    // @formatter:off
    @Shadow public abstract void save(boolean flush);
    @Shadow @Final ThreadedLevelLightEngine lightEngine;
    @Shadow @Final public ChunkMap chunkMap;
    @Shadow @Final
    ServerLevel level;
    @Shadow @Final private DistanceManager distanceManager;
    @Shadow protected abstract void clearCache();
    @Shadow @Nullable protected abstract ChunkHolder getVisibleChunkIfPresent(long chunkPosIn);

    @Shadow
    public abstract boolean runDistanceManagerUpdates();

    @Override
    public boolean isChunkLoaded(final int chunkX, final int chunkZ) {
        ChunkHolder chunk =  this.chunkMap.getUpdatingChunkIfPresent(ChunkPos.asLong(chunkX, chunkZ));
        return chunk != null &&  chunk.getFullChunkNow() != null;
    }

    public ThreadedLevelLightEngine a() {
        return this.lightEngine;
    }

    @Override
    public LevelChunk getChunkUnchecked(int chunkX, int chunkZ) {
        ChunkHolder chunk =  this.chunkMap.getUpdatingChunkIfPresent(ChunkPos.asLong(chunkX, chunkZ));
        if (chunk == null) {
            return null;
        }
        return chunk.getFullChunkNowUnchecked();
    }

    @ModifyVariable(method = "getChunkFutureMainThread", index = 4, at = @At("HEAD"), argsOnly = true)
    private boolean banner$skipIfUnloading(boolean flag, int chunkX, int chunkZ) {
        boolean currentlyUnloading = false;
        if (flag) {
            ChunkHolder chunkholder = this.getVisibleChunkIfPresent(ChunkPos.asLong(chunkX, chunkZ));
            if (chunkholder != null) {
                FullChunkStatus chunkStatus = ChunkLevel.fullStatus(chunkholder.oldTicketLevel);
                FullChunkStatus currentStatus = ChunkLevel.fullStatus(chunkholder.getTicketLevel());
                currentlyUnloading = (chunkStatus.isOrAfter(FullChunkStatus.FULL) && !currentStatus.isOrAfter(FullChunkStatus.FULL));
            }
            return !currentlyUnloading;
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
