package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.fabric.FabricInjectBukkit;
import com.mohistmc.banner.injection.world.level.InjectionLevel;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.CapturedBlockState;
import org.bukkit.craftbukkit.v1_19_R3.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.v1_19_R3.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_19_R3.generator.CustomWorldChunkManager;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftSpawnCategory;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Mixin(Level.class)
public abstract class MixinLevel implements LevelAccessor, AutoCloseable, InjectionLevel {

    // @formatter:off
    @Shadow @Final public boolean isClientSide;
    @Shadow @Final public Thread thread;
    @Shadow @Nullable public abstract BlockEntity getBlockEntity(BlockPos pos);
    @Shadow @Final private WorldBorder worldBorder;
    @Shadow public abstract WorldBorder getWorldBorder();
    @Shadow public abstract LevelChunk getChunk(int chunkX, int chunkZ);
    @Shadow public abstract LevelData getLevelData();
    @Shadow public abstract ResourceKey<Level> dimension();
    @Shadow public abstract DimensionType dimensionType();
    // @formatter:on

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
    private final AtomicReference<Boolean> banner$validate = new AtomicReference<>();

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

    @Override
    public CraftServer getCraftServer() {
        return (CraftServer) Bukkit.getServer();
    }

    @Override
    public SpigotWorldConfig bridge$spigotConfig() {
        return spigotConfig;
    }

    @Override
    public void notifyAndUpdatePhysics(BlockPos blockposition, LevelChunk chunk, BlockState oldBlock, BlockState newBlock, BlockState actualBlock, int i, int j) {
    }

    @Override
    public boolean addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        if (getWorld().getHandle() != (Object) this) {
            return getWorld().getHandle().addEntity(entity, reason);
        } else {
            this.pushAddEntityReason(reason);
            return this.addFreshEntity(entity);
        }
    }

    @Override
    public void pushAddEntityReason(CreatureSpawnEvent.SpawnReason reason) {
        if (getWorld().getHandle() != (Object) this) {
           getWorld().getHandle().pushAddEntityReason(reason);
        }
    }

    @Override
    public CreatureSpawnEvent.SpawnReason getAddEntityReason() {
        if (getWorld().getHandle() != (Object) this) {
            return getWorld().getHandle().getAddEntityReason();
        }
        return null;
    }

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
}

