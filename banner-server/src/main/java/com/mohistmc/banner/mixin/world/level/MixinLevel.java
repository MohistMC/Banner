package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import com.mohistmc.banner.asm.annotation.TransformAccess;
import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import com.mohistmc.banner.config.BannerWorldConfig;
import com.mohistmc.banner.fabric.BukkitRegistry;
import com.mohistmc.banner.fabric.WrappedWorlds;
import com.mohistmc.banner.injection.world.level.InjectionLevel;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CapturedBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.generator.CustomWorldChunkManager;
import org.bukkit.craftbukkit.util.CraftSpawnCategory;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Mixin(Level.class)
public abstract class MixinLevel implements LevelAccessor, AutoCloseable, InjectionLevel {

    // @formatter:off
    @Shadow @Final public boolean isClientSide;
    @Shadow @Final public Thread thread;
    @Shadow public abstract WorldBorder getWorldBorder();
    @Shadow public abstract LevelChunk getChunk(int chunkX, int chunkZ);
    @Shadow public abstract LevelData getLevelData();
    @Shadow public abstract ResourceKey<Level> dimension();
    @Shadow public abstract DimensionType dimensionType();
    @Shadow public abstract LevelChunk getChunkAt(BlockPos pos);
    @Shadow public abstract boolean isDebug();
    // @formatter:on

    @Shadow public abstract void setBlocksDirty(BlockPos blockPos, BlockState oldState, BlockState newState);

    @Shadow public abstract void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags);

    @Shadow public abstract void updateNeighbourForOutputSignal(BlockPos pos, Block block);

    @Shadow public abstract void onBlockStateChange(BlockPos pos, BlockState blockState, BlockState newState);

    @Shadow @Nullable public abstract MinecraftServer getServer();

    @Shadow @Final private ResourceKey<Level> dimension;
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
    public org.spigotmc.SpigotWorldConfig spigotConfig; // Spigot
    protected org.bukkit.World.Environment environment;
    protected org.bukkit.generator.BiomeProvider biomeProvider;
    private com.mohistmc.banner.config.BannerWorldConfig bannerConfig;
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static BlockPos lastPhysicsProblem; // Spigot

    @ShadowConstructor
    public void banner$constructor(WritableLevelData worldInfo, ResourceKey<Level> dimension, RegistryAccess registryAccess, final Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdate) {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void banner$constructor(WritableLevelData worldInfo, ResourceKey<Level> dimension, RegistryAccess registryAccess, final Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdate, org.bukkit.generator.ChunkGenerator gen, org.bukkit.generator.BiomeProvider biomeProvider, org.bukkit.World.Environment env) {
        banner$constructor(worldInfo, dimension, registryAccess, dimensionType, profiler, isRemote, isDebug, seed, maxNeighborUpdate);
        this.generator = gen;
        this.environment = env;
        this.biomeProvider = biomeProvider;
        banner$initWorld(null);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(WritableLevelData info, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdates, CallbackInfo ci) {
        if ((Object) this instanceof ServerLevel) {
            this.banner$setSpigotConfig(new SpigotWorldConfig(BukkitMethodHooks.getServer().storageSource.getDimensionPath(dimension).getFileName().toFile().getName()));
            this.banner$setBannerConfig(new BannerWorldConfig(BukkitMethodHooks.getServer().storageSource.getDimensionPath(dimension).getFileName().toFile().getName()));
        }
        for (SpawnCategory spawnCategory : SpawnCategory.values()) {
            if (CraftSpawnCategory.isValidForLimits(spawnCategory)) {
                this.ticksPerSpawnCategory.put(spawnCategory, this.getCraftServer().getTicksPerSpawns(spawnCategory));
            }
        }
        // CraftBukkit start
        getWorldBorder().banner$setWorld(((Level) (Object) this));
        // From PlayerList.setPlayerFileData
        getWorldBorder().addListener(new BorderChangeListener() {
            @Override
            public void onBorderSizeSet(WorldBorder worldborder, double d0) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderSizePacket(worldborder), worldborder.bridge$world());
            }

            @Override
            public void onBorderSizeLerping(WorldBorder worldborder, double d0, double d1, long i) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderLerpSizePacket(worldborder), worldborder.bridge$world());
            }

            @Override
            public void onBorderCenterSet(WorldBorder worldborder, double d0, double d1) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderCenterPacket(worldborder), worldborder.bridge$world());
            }

            @Override
            public void onBorderSetWarningTime(WorldBorder worldborder, int i) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderWarningDelayPacket(worldborder), worldborder.bridge$world());
            }

            @Override
            public void onBorderSetWarningBlocks(WorldBorder worldborder, int i) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderWarningDistancePacket(worldborder), worldborder.bridge$world());
            }

            @Override
            public void onBorderSetDamagePerBlock(WorldBorder worldborder, double d0) {}

            @Override
            public void onBorderSetDamageSafeZOne(WorldBorder worldborder, double d0) {}
        });
        // CraftBukkit end
    }

    @Redirect(method = "<init>", at = @At(value = "NEW",
            args = "class=net/minecraft/world/level/border/WorldBorder"))
    private WorldBorder banner$resetBorder0() {
        return new WorldBorder() {
            public double getCenterX() {
                return super.getCenterX(); // CraftBukkit
            }

            public double getCenterZ() {
                return super.getCenterZ(); // CraftBukkit
            }
        };
    }

    @Override
    public CraftWorld getWorld() {
        banner$initWorld(null);
        return this.world;
    }

    private AtomicBoolean captured = new AtomicBoolean(false);

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    private void banner$captureTree(BlockPos blockPos, BlockState blockState, int i, int j, CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start - tree generation
        if (this.captureTreeGeneration) {
            CapturedBlockState blockstate = capturedBlockStates.get(blockPos);
            if (blockstate == null) {
                blockstate = CapturedBlockState.getTreeBlockState(((Level) (Object) this), blockPos, i);
                this.capturedBlockStates.put(blockPos.immutable(), blockstate);
            }
            blockstate.setData(blockState);
            cir.setReturnValue(true);
        }
        Entity entityChangeBlock = BukkitSnapshotCaptures.getEntityChangeBlock();
        if (entityChangeBlock != null && !CraftEventFactory.callEntityChangeBlockEvent(entityChangeBlock, blockPos, blockState)) {
            cir.setReturnValue(false);
        }
        // CraftBukkit end
    }

    private AtomicReference<BlockState> banner$state = new AtomicReference<>();

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$captrueBlock(BlockPos blockPos, BlockState blockState, int i, int j,
                                     CallbackInfoReturnable<Boolean> cir, LevelChunk levelChunk, Block block) {
        // CraftBukkit start - capture blockstates
        if (this.captureBlockStates && !this.capturedBlockStates.containsKey(blockPos)) {
            CapturedBlockState blockstate = CapturedBlockState.getBlockState(((Level) (Object) this), blockPos, i);
            this.capturedBlockStates.put(blockPos.immutable(), blockstate);
            captured.set(true);
        }
        // CraftBukkit end
        BlockState banner$blockState2 = levelChunk.setBlockState(blockPos, blockState, (i & 64) != 0, (i & 1024) == 0); // CraftBukkit custom NO_PLACE flag
        banner$state.set(banner$blockState2);
    }

    @Redirect(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState banner$resetState(LevelChunk levelChunk, BlockPos blockPos, BlockState blockState, boolean bl) {
        BlockState banner$state1 = banner$state.get();
        banner$state1 = banner$state1 == null ? levelChunk.setBlockState(blockPos, blockState, bl) : banner$state1;
        BlockState banner$state2 = banner$state1;
        if (banner$state2 == null && captured.get()) {
            this.capturedBlockStates.remove(blockPos);
        }
        return banner$state2;
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", shift = At.Shift.AFTER),
            cancellable = true)
    private void banner$finalCapture(BlockPos blockPos, BlockState blockState, int i, int j, CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start
        if (this.captureBlockStates) { // Don't notify clients or update physics while capturing blockstates
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;onBlockStateChange(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V"),
            cancellable = true)
    private void banner$checkState(BlockPos blockPos, BlockState blockState, int i, int j, CallbackInfoReturnable<Boolean> cir) {
        if (preventPoiUpdated) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;updateNeighbourShapes(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;II)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$physicEvent(BlockPos blockPos, BlockState blockState, int i, int j, CallbackInfoReturnable<Boolean> cir, LevelChunk levelChunk, Block block, BlockState blockState2, BlockState blockState3, int k) {
        CraftWorld world = ((ServerLevel) (Object) this).getWorld();
        if (world != null) {
            BlockPhysicsEvent event = new BlockPhysicsEvent(world.getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ()), CraftBlockData.fromData(blockState));
            this.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                cir.setReturnValue(true);
                cir.cancel();
            }
            // CraftBukkit end
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

            if ((i & 2) != 0 && (!this.isClientSide || (i & 4) == 0) && (this.isClientSide || chunk == null || (chunk.getFullStatus() != null && chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING)))) { // allow chunk to be null here as chunk.isReady() is false when we send our notification during block placement
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
                try {
                    if (world != null) {
                        BlockPhysicsEvent event = new BlockPhysicsEvent(world.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), CraftBlockData.fromData(iblockdata));
                        this.getCraftServer().getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            return;
                        }
                    }
                } catch (StackOverflowError e) {
                    lastPhysicsProblem = blockposition;
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

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    private void banner$isThundering(CallbackInfoReturnable<Boolean> cir) {
        if (spigotConfig != null && spigotConfig.thunderChance <= 0) {
            cir.setReturnValue(false);
        }
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
    public boolean bridge$keepSpawnInMemory() {
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
    public BannerWorldConfig bridge$bannerConfig() {
        return bannerConfig;
    }

    @Override
    public void banner$setBannerConfig(BannerWorldConfig bannerWorldConfig) {
        this.bannerConfig = bannerWorldConfig;
    }

    @Override
    public boolean bridge$preventPoiUpdated() {
        return preventPoiUpdated;
    }

    @Override
    public void banner$setPreventPoiUpdated(boolean preventPoiUpdated) {
        this.preventPoiUpdated = preventPoiUpdated;
    }

    @Override
    public CraftWorld banner$initWorld(@Nullable LevelStem levelStem) {
        var newStem = levelStem;
        if (this.world == null) {
            Optional<Field> delegate = WrappedWorlds.getDelegate(this.getClass());
            if (delegate.isPresent()) {
                try {
                    return ((Level) delegate.get().get(this)).getWorld();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            if (environment == null) {
                environment = BukkitRegistry.environment.getOrDefault(getTypeKey(), World.Environment.CUSTOM);
            }
            if (generator == null) {
                generator = getCraftServer().getGenerator(((ServerLevelData) this.getLevelData()).getLevelName());
                if (generator != null && (Object) this instanceof ServerLevel serverWorld) {
                    org.bukkit.generator.WorldInfo worldInfo = new CraftWorldInfo((ServerLevelData) getLevelData(),
                            ((ServerLevel) (Object) this).bridge$convertable(), environment, this.dimensionType());
                    if (biomeProvider == null && generator != null) {
                        biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
                    }
                    var generator = serverWorld.getChunkSource().getGenerator();
                    if (biomeProvider != null) {
                        BiomeSource biomeSource = new CustomWorldChunkManager(worldInfo, biomeProvider, serverWorld.registryAccess().registryOrThrow(Registries.BIOME));
                        if (generator instanceof NoiseBasedChunkGenerator cga) {
                            generator = new NoiseBasedChunkGenerator(biomeSource, cga.settings);
                        } else if (generator instanceof FlatLevelSource cpf) {
                            var flatLevelSource = new FlatLevelSource(cpf.settings());
                            flatLevelSource.banner$setBiomeSource(biomeSource);
                            generator = flatLevelSource;
                        }
                    }
                    if (((Level) (Object) this) instanceof ServerLevel) {
                        var banner$gen = newStem.generator();
                        banner$gen = new CustomChunkGenerator(serverWorld, generator, this.generator);
                    }
                }
            }
            this.world = new CraftWorld((ServerLevel) (Object) this, generator, biomeProvider, environment);
            getCraftServer().addWorld(this.world);
        }
        return this.world;
    }
}

