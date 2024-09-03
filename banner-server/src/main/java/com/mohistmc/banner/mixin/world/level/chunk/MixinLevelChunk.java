package com.mohistmc.banner.mixin.world.level.chunk;

import com.mohistmc.banner.bukkit.DistValidate;
import com.mohistmc.banner.injection.world.level.chunk.InjectionLevelChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk extends ChunkAccess implements InjectionLevelChunk {

    public MixinLevelChunk(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @org.jetbrains.annotations.Nullable LevelChunkSection[] levelChunkSections, @org.jetbrains.annotations.Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, l, levelChunkSections, blendingData);
    }

    // @formatter:off
    @Shadow @Nullable public abstract BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving);
    @Mutable @Shadow @Final public Level level;
    // @formatter:on

    @Shadow public abstract Level getLevel();

    // CraftBukkit start
    public org.bukkit.Chunk bukkitChunk;

    public boolean mustNotSave;
    public boolean needsDecoration;
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
    public CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer( DATA_TYPE_REGISTRY );
    public AtomicBoolean banner$doPlace = new AtomicBoolean(true);
    public ServerLevel r;

    @Override
    public ServerLevel banner$r() {
        return r;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V", at = @At("RETURN"))
    private void banner$init(Level worldIn, ChunkPos p_196855_, UpgradeData p_196856_, LevelChunkTicks<Block> p_196857_, LevelChunkTicks<Fluid> p_196858_, long p_196859_, @Nullable LevelChunkSection[] p_196860_, @Nullable LevelChunk.PostLoadProcessor p_196861_, @Nullable BlendingData p_196862_, CallbackInfo ci) {
        if (DistValidate.isValid(worldIn)) {
            this.r = ((ServerLevel) worldIn);
        }
        if (p_196855_ != null) this.bukkitChunk = new CraftChunk((LevelChunk) (Object) this);
    }

    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V", at = @At("RETURN"))
    private void banner$init(ServerLevel p_196850_, ProtoChunk protoChunk, @Nullable LevelChunk.PostLoadProcessor p_196852_, CallbackInfo ci) {
        this.needsDecoration = true; // CraftBukkit
        this.persistentDataContainer = protoChunk.bridge$persistentDataContainer(); // SPIGOT-6814: copy PDC to account for 1.17 to 1.18 chunk upgrading.
    }

    @Inject(method = "removeBlockEntity", at = @At(value = "INVOKE_ASSIGN", remap = false, target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"))
    private void banner$remove(BlockPos pos, CallbackInfo ci) {
        if (!pendingBlockEntities.isEmpty()) {
            pendingBlockEntities.remove(pos);
        }
    }

    @Override
    public org.bukkit.Chunk getBukkitChunk() {
        return bukkitChunk;
    }

    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving, boolean doPlace) {
        this.banner$doPlace.set(doPlace);
        try {
            return this.setBlockState(pos, state, isMoving);
        } finally {
            this.banner$doPlace.set(true);
        }
    }

    @Override
    public void loadCallback() {
        org.bukkit.Server server = Bukkit.getServer();
        if (server != null && r != null) {
            /*
             * If it's a new world, the first few chunks are generated inside
             * the World constructor. We can't reliably alter that, so we have
             * no way of creating a CraftWorld/CraftServer at that point.
             */

            var bukkitChunk = new CraftChunk((LevelChunk) (Object) this);
            server.getPluginManager().callEvent(new ChunkLoadEvent(bukkitChunk, this.needsDecoration));

            if (this.needsDecoration) {
                this.needsDecoration = false;
                java.util.Random random = new java.util.Random();
                random.setSeed(this.r.getSeed());
                long xRand = random.nextLong() / 2L * 2L + 1L;
                long zRand = random.nextLong() / 2L * 2L + 1L;
                random.setSeed((long) this.chunkPos.x * xRand + (long) this.chunkPos.z * zRand ^ this.r.getSeed());

                org.bukkit.World world = this.r.getWorld();
                if (world != null) {
                    this.r.banner$setPopulating(true);
                    try {
                        for (org.bukkit.generator.BlockPopulator populator : world.getPopulators()) {
                            populator.populate(world, random, bukkitChunk);
                        }
                    } finally {
                        this.r.banner$setPopulating(false);
                    }
                }
                server.getPluginManager().callEvent(new org.bukkit.event.world.ChunkPopulateEvent(bukkitChunk));
            }
        }
    }

    @Override
    public void unloadCallback() {
        if (this.r != null) {
            org.bukkit.Server server = this.getLevel().getCraftServer();
            var bukkitChunk = new CraftChunk((LevelChunk) (Object) this);
            org.bukkit.event.world.ChunkUnloadEvent unloadEvent = new org.bukkit.event.world.ChunkUnloadEvent(bukkitChunk, this.isUnsaved());
            server.getPluginManager().callEvent(unloadEvent);
            // note: saving can be prevented, but not forced if no saving is actually required
            this.mustNotSave = !unloadEvent.isSaveChunk();
        }
    }

    @Redirect(method = "setBlockState", at = @At(value = "FIELD", ordinal = 1, target = "Lnet/minecraft/world/level/Level;isClientSide:Z"))
    public boolean banner$redirectIsRemote(Level world) {
        return world.isClientSide && !this.banner$doPlace.getAndSet(true);
    }

    @Override
    public boolean isUnsaved() {
        return super.isUnsaved() && !this.mustNotSave;
    }
}
