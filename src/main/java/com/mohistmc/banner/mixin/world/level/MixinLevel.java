package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.fabric.FabricInjectBukkit;
import com.mohistmc.banner.injection.world.level.InjectionLevel;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.CapturedBlockState;
import org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R3.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.v1_19_R3.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_19_R3.generator.CustomWorldChunkManager;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftSpawnCategory;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(Level.class)
public abstract class MixinLevel implements LevelAccessor, AutoCloseable, InjectionLevel {

    // @formatter:off
    @Shadow @Final public boolean isClientSide;
    @Shadow @Final public Thread thread;
    @Shadow @Final private WorldBorder worldBorder;
    @Shadow public abstract WorldBorder getWorldBorder();
    @Shadow public abstract LevelChunk getChunk(int chunkX, int chunkZ);
    @Shadow public abstract LevelData getLevelData();
    @Shadow public abstract ResourceKey<Level> dimension();
    @Shadow public abstract DimensionType dimensionType();
    @Shadow public abstract LevelChunk getChunkAt(BlockPos pos);
    @Shadow public abstract boolean isDebug();
    // @formatter:on

    @Shadow public abstract ProfilerFiller getProfiler();

    @Shadow public abstract void setBlocksDirty(BlockPos blockPos, BlockState oldState, BlockState newState);

    @Shadow public abstract void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags);

    @Shadow public abstract void updateNeighbourForOutputSignal(BlockPos pos, Block block);

    @Shadow public abstract void onBlockStateChange(BlockPos pos, BlockState blockState, BlockState newState);

    private CraftWorld world;
    public boolean pvpMode;
    public boolean keepSpawnInMemory = true;
    public org.bukkit.generator.ChunkGenerator generator;

    public boolean preventPoiUpdated = false; // CraftBukkit - SPIGOT-5710
    public boolean captureBlockStates = false;
    public boolean captureTreeGeneration = false;
    public Map<BlockPos, CapturedBlockState> capturedBlockStates = new java.util.LinkedHashMap<>();
    public Map<BlockPos, BlockEntity> capturedTileEntities = new HashMap<>();
    public List<ItemEntity> captureDrops;
    public final it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<SpawnCategory> ticksPerSpawnCategory = new it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<>();
    public boolean populating;
    private org.spigotmc.SpigotWorldConfig spigotConfig; // Spigot
    protected org.bukkit.World.Environment environment;
    protected org.bukkit.generator.BiomeProvider biomeProvider;

    public void banner$constructor(WritableLevelData worldInfo, ResourceKey<Level> dimension, RegistryAccess registryAccess, final Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdate) {
        throw new RuntimeException();
    }

    public void banner$constructor(WritableLevelData worldInfo, ResourceKey<Level> dimension, RegistryAccess registryAccess, final Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdate, org.bukkit.generator.ChunkGenerator gen, org.bukkit.generator.BiomeProvider biomeProvider, org.bukkit.World.Environment env) {
        banner$constructor(worldInfo, dimension, registryAccess, dimensionType, profiler, isRemote, isDebug, seed, maxNeighborUpdate);
        this.generator = gen;
        this.environment = env;
        this.biomeProvider = biomeProvider;
        getWorld();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(WritableLevelData info, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdates, CallbackInfo ci) {
        for (SpawnCategory spawnCategory : SpawnCategory.values()) {
            if (CraftSpawnCategory.isValidForLimits(spawnCategory)) {
                this.ticksPerSpawnCategory.put(spawnCategory, this.getCraftServer().getTicksPerSpawns(spawnCategory));
            }
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void banner$handleWorldFolder(WritableLevelData writableLevelData, ResourceKey resourceKey, RegistryAccess registryAccess, Holder holder, Supplier supplier, boolean bl, boolean bl2, long l, int i, CallbackInfo ci) {
        if ((((Level) (Object) this) instanceof ServerLevel)) {
            ServerLevel nms = ((ServerLevel) (Object) this);
            String name = ((ServerLevelData) nms.getLevelData()).getLevelName();

            File fi = new File(name + "_the_end");
            File van = new File(new File(name), "DIM1");

            if (fi.exists()) {
                File dim = new File(fi, "DIM1");
                if (dim.exists()) {
                    BannerServer.LOGGER.info("------ Migration of world file: " + name + "_the_end !");
                    BannerServer.LOGGER.info("Banner is currently migrating the world back to the vanilla format!");
                    BannerServer.LOGGER.info("Do to the differences between Spigot & Fabric world folders, we require migration.");
                    if (dim.renameTo(van)) {
                        BannerServer.LOGGER.info("---- Migration of old bukkit format folder complete ----");
                    } else {
                        BannerServer.LOGGER.info("---- Migration of old bukkit format folder FAILED! ----");
                    }
                    fi.delete();
                }
            }

            File fi2 = new File(name + "_nether");
            File van2 = new File(new File(name), "DIM-1");

            if (fi2.exists()) {
                File dim = new File(fi2, "DIM-1");
                if (dim.exists()) {
                    BannerServer.LOGGER.info("------ Migration of world file: " + fi2.getName() + " !");
                    BannerServer.LOGGER.info("Banner is currently migrating the world back to the vanilla format!");
                    BannerServer.LOGGER.info("Do to the differences between Spigot & Fabric world folders, we require migration.");
                    if (dim.renameTo(van2)) {
                        BannerServer.LOGGER.info("---- Migration of old bukkit format folder complete ----");
                    } else {
                        BannerServer.LOGGER.info("---- Migration of old bukkit format folder FAILED! ----");
                    }
                    fi.delete();
                }
            }
        }
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/DamageSources;<init>(Lnet/minecraft/core/RegistryAccess;)V",
            shift = At.Shift.AFTER))
    private void banner$addWorldBorder(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder holder, Supplier supplier, boolean bl, boolean bl2, long l, int i, CallbackInfo ci) {
        this.getWorldBorder().banner$setWorld((ServerLevel) (Object) this);
        // CraftBukkit start
        getWorldBorder().addListener(new BorderChangeListener() {
            @Override
            public void onBorderSizeSet(WorldBorder border, double size) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderSizePacket(worldBorder), worldBorder.bridge$world());
            }

            @Override
            public void onBorderSizeLerping(WorldBorder border, double oldSize, double newSize, long time) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderLerpSizePacket(worldBorder), worldBorder.bridge$world());
            }

            @Override
            public void onBorderCenterSet(WorldBorder border, double x, double z) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderCenterPacket(worldBorder), worldBorder.bridge$world());
            }

            @Override
            public void onBorderSetWarningTime(WorldBorder border, int warningTime) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderWarningDelayPacket(worldBorder), worldBorder.bridge$world());
            }

            @Override
            public void onBorderSetWarningBlocks(WorldBorder border, int warningBlocks) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderWarningDistancePacket(worldBorder), worldBorder.bridge$world());
            }

            @Override
            public void onBorderSetDamagePerBlock(WorldBorder border, double damagePerBlock) {
            }

            @Override
            public void onBorderSetDamageSafeZOne(WorldBorder border, double damageSafeZone) {}
        });
    }

    @Override
    public CraftWorld getWorld() {
        if (this.world == null) {
            if (environment == null) {
                environment = FabricInjectBukkit.DIM_MAP.getOrDefault(this.getTypeKey(), World.Environment.CUSTOM);
            }
            if (generator == null) {
                generator = getCraftServer().getGenerator(((ServerLevelData) this.getLevelData()).getLevelName());
                if (generator != null && ((Level) (Object) this) instanceof ServerLevel serverWorld) {
                    org.bukkit.generator.WorldInfo worldInfo = new CraftWorldInfo((ServerLevelData) getLevelData(),
                            ((ServerLevel) (Object) this).bridge$convertable(), environment, this.dimensionType());
                    if (biomeProvider == null && generator != null) {
                        biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
                    }
                    var generator = serverWorld.getChunkSource().getGenerator();
                    if (biomeProvider != null) {
                        generator.biomeSource = new CustomWorldChunkManager(worldInfo, biomeProvider, serverWorld.registryAccess().registryOrThrow(Registries.BIOME));
                    }
                    serverWorld.getChunkSource().chunkMap.generator = new CustomChunkGenerator(serverWorld, generator, this.generator);
                }
            }
            this.world = new CraftWorld((ServerLevel) (Object) this, generator, biomeProvider, environment);
            getCraftServer().addWorld(this.world);
        }
        return this.world;
    }

    /**
     * @author wdog5
     * @reason functionality replaced
     * TODO inline this with injects
     */
    @Overwrite
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        // CraftBukkit start - tree generation
        if (this.captureTreeGeneration) {
            CapturedBlockState blockstate = capturedBlockStates.get(pos);
            if (blockstate == null) {
                blockstate = CapturedBlockState.getTreeBlockState(((Level) (Object) this), pos, flags);
                this.capturedBlockStates.put(pos.immutable(), blockstate);
            }
            blockstate.setData(state);
            return true;
        }
        // CraftBukkit end
        if (this.isOutsideBuildHeight(pos)) {
            return false;
        } else if (!this.isClientSide && this.isDebug()) {
            return false;
        } else {
            LevelChunk levelChunk = this.getChunkAt(pos);
            Block block = state.getBlock();
            // CraftBukkit start - capture blockstates
            boolean captured = false;
            if (this.captureBlockStates && !this.capturedBlockStates.containsKey(pos)) {
                CapturedBlockState blockstate = CapturedBlockState.getBlockState(((Level) (Object) this), pos, flags);
                this.capturedBlockStates.put(pos.immutable(), blockstate);
                captured = true;
            }
            // CraftBukkit end
            BlockState blockState = levelChunk.setBlockState(pos, state, (flags & 64) != 0, (flags & 1024) == 0); // CraftBukkit custom NO_PLACE flag
            if (blockState == null) {
                // CraftBukkit start - remove blockstate if failed (or the same)
                if (this.captureBlockStates && captured) {
                    this.capturedBlockStates.remove(pos);
                }
                // CraftBukkit end
                return false;
            } else {
                BlockState blockState2 = this.getBlockState(pos);
                if ((flags & 128) == 0 && blockState2 != blockState && (blockState2.getLightBlock(this, pos) != blockState.getLightBlock(this, pos) || blockState2.getLightEmission() != blockState.getLightEmission() || blockState2.useShapeForLightOcclusion() || blockState.useShapeForLightOcclusion())) {
                    this.getProfiler().push("queueCheckLight");
                    this.getChunkSource().getLightEngine().checkBlock(pos);
                    this.getProfiler().pop();
                }
                // CraftBukkit start
               if (!this.captureBlockStates) { // Don't notify clients or update physics while capturing blockstates
                    // Modularize client and physic updates
                   // Banner start - copy method content to this to compat mixins
                   BlockState iblockdata = state;
                   BlockState iblockdata1 = blockState;
                   BlockState iblockdata2 = blockState2;
                   if (iblockdata2 == iblockdata) {
                       if (iblockdata1 != iblockdata2) {
                           this.setBlocksDirty(pos, iblockdata1, iblockdata2);
                       }

                       if ((flags & 2) != 0 && (!this.isClientSide || (flags & 4) == 0) && (this.isClientSide || levelChunk == null || (levelChunk.getFullStatus() != null && levelChunk.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING)))) { // allow chunk to be null here as chunk.isReady() is false when we send our notification during block placement
                           this.sendBlockUpdated(pos, iblockdata1, iblockdata, flags);
                       }

                       if ((flags & 1) != 0) {
                           this.blockUpdated(pos, iblockdata1.getBlock());
                           if (!this.isClientSide && iblockdata.hasAnalogOutputSignal()) {
                               this.updateNeighbourForOutputSignal(pos, state.getBlock());
                           }
                       }

                       if ((flags & 16) == 0 && recursionLeft > 0) {
                           int k = flags & -34;

                           // CraftBukkit start
                           iblockdata1.updateIndirectNeighbourShapes(((Level) (Object) this), pos, k, recursionLeft - 1); // Don't call an event for the old block to limit event spam
                           CraftWorld world = ((ServerLevel) (Object) this).getWorld();
                           if (world != null) {
                               BlockPhysicsEvent event = new BlockPhysicsEvent(world.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), CraftBlockData.fromData(iblockdata));
                               this.getCraftServer().getPluginManager().callEvent(event);

                               if (event.isCancelled()) {
                                   return false;
                               }
                           }
                           // CraftBukkit end
                           iblockdata.updateNeighbourShapes(((Level) (Object) this), pos, k, recursionLeft - 1);
                           iblockdata.updateIndirectNeighbourShapes(((Level) (Object) this), pos, k, recursionLeft - 1);
                       }

                       // CraftBukkit start - SPIGOT-5710
                       if (!preventPoiUpdated) {
                           this.onBlockStateChange(pos, iblockdata1, iblockdata2);
                       }
                       // CraftBukkit end
                   }                }
                // CraftBukkit end
                return true;
            }
            // Banner end
        }
    }

    @Override
    public void notifyAndUpdatePhysics(BlockPos blockposition, LevelChunk chunk, BlockState oldBlock, BlockState newBlock, BlockState actualBlock, int i, int j) {
        BlockState iblockdata = newBlock;
        BlockState iblockdata1 = oldBlock;
        BlockState iblockdata2 = actualBlock;
        if (iblockdata2 == iblockdata) {
            if (iblockdata1 != iblockdata2) {
                this.setBlocksDirty(blockposition, iblockdata1, iblockdata2);
            }

            if ((i & 2) != 0 && (!this.isClientSide || (i & 4) == 0) && (this.isClientSide || chunk == null || (chunk.getFullStatus() != null && chunk.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING)))) { // allow chunk to be null here as chunk.isReady() is false when we send our notification during block placement
                this.sendBlockUpdated(blockposition, iblockdata1, iblockdata, i);
            }

            if ((i & 1) != 0) {
                this.blockUpdated(blockposition, iblockdata1.getBlock());
                if (!this.isClientSide && iblockdata.hasAnalogOutputSignal()) {
                    this.updateNeighbourForOutputSignal(blockposition, newBlock.getBlock());
                }
            }

            if ((i & 16) == 0 && j > 0) {
                int k = i & -34;

                // CraftBukkit start
                iblockdata1.updateIndirectNeighbourShapes(((Level) (Object) this), blockposition, k, j - 1); // Don't call an event for the old block to limit event spam
                CraftWorld world = ((ServerLevel) (Object) this).getWorld();
                if (world != null) {
                    BlockPhysicsEvent event = new BlockPhysicsEvent(world.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), CraftBlockData.fromData(iblockdata));
                    this.getCraftServer().getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        return;
                    }
                }
                // CraftBukkit end
                iblockdata.updateNeighbourShapes(((Level) (Object) this), blockposition, k, j - 1);
                iblockdata.updateIndirectNeighbourShapes(((Level) (Object) this), blockposition, k, j - 1);
            }

            // CraftBukkit start - SPIGOT-5710
            if (!preventPoiUpdated) {
                this.onBlockStateChange(blockposition, iblockdata1, iblockdata2);
            }
            // CraftBukkit end
        }
    }

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void banner$addCaptureCheck(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        // CraftBukkit start - tree generation
        if (captureTreeGeneration) {
            CapturedBlockState previous = capturedBlockStates.get(pos);
            if (previous != null) {
                cir.setReturnValue(previous.getHandle());
            }
        }
        // CraftBukkit end
    }

    @Inject(method = "setBlockEntity",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getChunkAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/chunk/LevelChunk;",
            shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$addCaptureCheck0(BlockEntity blockEntity, CallbackInfo ci, BlockPos blockPos) {
        // CraftBukkit start
        if (captureBlockStates) {
            capturedTileEntities.put(blockPos.immutable(), blockEntity);
            ci.cancel();
        }
        // CraftBukkit end
    }

    @Override
    public CraftServer getCraftServer() {
        return (CraftServer) Bukkit.getServer();
    }

    @Override
    public SpigotWorldConfig bridge$spigotConfig() {
        return spigotConfig;
    }

    /**
     * @author wdog5
     * @reason functionality replaced
     * TODO inline this with injects
     */
    @Overwrite
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return getBlockEntity(pos, true);
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos blockposition, boolean validate) {
        // CraftBukkit start
        if (capturedTileEntities.containsKey(blockposition)) {
            return capturedTileEntities.get(blockposition);
        }
        // CraftBukkit end
        if (this.isOutsideBuildHeight(blockposition)) {
            return null;
        } else {
            return !this.isClientSide && Thread.currentThread() != this.thread ? null : this.getChunkAt(blockposition).getBlockEntity(blockposition, LevelChunk.EntityCreationType.IMMEDIATE);
        }
    }

    @Override
    public boolean addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        if (getWorld().getHandle() != ((Level) (Object) this)) {
            return getWorld().getHandle().addEntity(entity, reason);
        } else {
            this.pushAddEntityReason(reason);
            return this.addFreshEntity(entity);
        }
    }

    @Override
    public void pushAddEntityReason(CreatureSpawnEvent.SpawnReason reason) {
        if (getWorld().getHandle() != ((Level) (Object) this)) {
           getWorld().getHandle().pushAddEntityReason(reason);
        }
    }

    @Override
    public CreatureSpawnEvent.SpawnReason getAddEntityReason() {
        if (getWorld().getHandle() != ((Level) (Object) this)) {
            return getWorld().getHandle().getAddEntityReason();
        }
        return null;
    }

    @Override
    public abstract ResourceKey<LevelStem> getTypeKey();

    @Override
    public BiomeProvider bridge$biomeProvider() {
        return biomeProvider;
    }

    @Override
    public void banner$setBiomeProvider(BiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }

    @Override
    public World.Environment bridge$environment() {
        return environment;
    }

    @Override
    public void banner$setEnvironment(World.Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean bridge$pvpMode() {
        return this.pvpMode;
    }

    @Override
    public void banner$setPvpMode(boolean pvpMode) {
        this.pvpMode = pvpMode;
    }

    @Override
    public boolean bridge$captureBlockStates() {
        return this.captureBlockStates;
    }

    @Override
    public void banner$setCaptureBlockStates(boolean captureState) {
        this.captureBlockStates = captureState;
    }

    @Override
    public boolean bridge$captureTreeGeneration() {
        return this.captureTreeGeneration;
    }

    @Override
    public void banner$setCaptureTreeGeneration(boolean treeGeneration) {
        this.captureTreeGeneration = treeGeneration;
    }

    @Override
    public Map<BlockPos, CapturedBlockState> bridge$capturedBlockStates() {
        return this.capturedBlockStates;
    }

    @Override
    public Map<BlockPos, BlockEntity> bridge$capturedTileEntities() {
        return capturedTileEntities;
    }

    @Override
    public void banner$setCapturedTileEntities(Map<BlockPos, BlockEntity> tileEntities) {
        this.capturedTileEntities = tileEntities;
    }

    @Override
    public void banner$setCapturedBlockStates(Map<BlockPos, CapturedBlockState> capturedBlockStates) {
        this.capturedBlockStates = capturedBlockStates;
    }

    @Override
    public List<ItemEntity> bridge$captureDrops() {
        return this.captureDrops;
    }

    @Override
    public void banner$setCaptureDrops(List<ItemEntity> captureDrops) {
        this.captureDrops = captureDrops;
    }

    @Override
    public Object2LongOpenHashMap<SpawnCategory> bridge$ticksPerSpawnCategory() {
        return this.ticksPerSpawnCategory;
    }

    @Override
    public boolean bridge$populating() {
        return populating;
    }

    @Override
    public void banner$setPopulating(boolean populating) {
        this.populating = populating;
    }

    @Override
    public boolean bridge$KeepSpawnInMemory() {
        return keepSpawnInMemory;
    }

    @Override
    public void banner$setKeepSpawnInMemory(boolean keepSpawnInMemory) {
        this.keepSpawnInMemory = keepSpawnInMemory;
    }

    @Override
    public ChunkGenerator bridge$generator() {
        return generator;
    }

    @Override
    public void banner$setGenerator(ChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void banner$setSpigotConfig(SpigotWorldConfig spigotWorldConfig) {
        this.spigotConfig = spigotWorldConfig;
    }

    @Override
    public boolean bridge$preventPoiUpdated() {
        return preventPoiUpdated;
    }

    @Override
    public void banner$setPreventPoiUpdated(boolean preventPoiUpdated) {
        this.preventPoiUpdated = preventPoiUpdated;
    }
}

