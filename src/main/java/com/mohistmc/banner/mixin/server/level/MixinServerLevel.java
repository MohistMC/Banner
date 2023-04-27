package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionServerLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.ticks.LevelTicks;
import org.bukkit.craftbukkit.v1_19_R3.util.WorldUUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level implements InjectionServerLevel {

    @Shadow public abstract LevelTicks<Block> getBlockTicks();
    public LevelStorageSource.LevelStorageAccess convertable;
    public UUID uuid;
    public PrimaryLevelData serverLevelDataCB;
    public ResourceKey<LevelStem> typeKey;

    protected MixinServerLevel(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, supplier, bl, bl2, l, i);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(MinecraftServer minecraftServer, Executor backgroundExecutor, LevelStorageSource.LevelStorageAccess levelSave, ServerLevelData worldInfo, ResourceKey<Level> dimension, LevelStem levelStem, ChunkProgressListener statusListener, boolean isDebug, long seed, List<CustomSpawner> specialSpawners, boolean shouldBeTicking, CallbackInfo ci) {
        this.uuid = WorldUUID.getUUID(levelSave.getDimensionPath(this.dimension()).toFile());
    }

    @Override
    public PrimaryLevelData bridge$serverLevelDataCB() {
        return serverLevelDataCB;
    }

    @Override
    public LevelStorageSource.LevelStorageAccess bridge$convertable() {
        return convertable;
    }

    @Override
    public ResourceKey<LevelStem> getTypeKey() {
        return typeKey;
    }

    @Override
    public UUID bridge$uuid() {
        return uuid;
    }
}
