package com.mohistmc.banner.mixin.world.level.chunk;

import com.mohistmc.banner.bukkit.DistValidate;
import com.mohistmc.banner.injection.world.level.chunk.InjectionLevelChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftChunk;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk extends ChunkAccess implements InjectionLevelChunk {

    public MixinLevelChunk(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @org.jetbrains.annotations.Nullable LevelChunkSection[] levelChunkSections, @org.jetbrains.annotations.Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, l, levelChunkSections, blendingData);
    }

    // @formatter:off
    @Shadow @Nullable public abstract BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving);
    @Mutable @Shadow @Final public Level level;
    // @formatter:on

    @Shadow @org.jetbrains.annotations.Nullable protected abstract BlockEntity promotePendingBlockEntity(BlockPos pos, CompoundTag tag);

    @Shadow @org.jetbrains.annotations.Nullable protected abstract BlockEntity createBlockEntity(BlockPos pos);

    @Shadow public abstract void addAndRegisterBlockEntity(BlockEntity blockEntity);

    public boolean mustNotSave;
    public boolean needsDecoration;
    private transient boolean banner$doPlace;
    public ServerLevel q; // TODO check on update
    private AtomicReference<Block> banner$block = new AtomicReference<>();

    @Redirect(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunk;level:Lnet/minecraft/world/level/Level;"))
    private void banner$setServerLevel(LevelChunk instance, Level level) {
        this.level = (ServerLevel) level;// CraftBukkit - type
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V", at = @At("RETURN"))
    private void banner$init(Level worldIn, ChunkPos p_196855_, UpgradeData p_196856_, LevelChunkTicks<Block> p_196857_, LevelChunkTicks<Fluid> p_196858_, long p_196859_, @Nullable LevelChunkSection[] p_196860_, @Nullable LevelChunk.PostLoadProcessor p_196861_, @Nullable BlendingData p_196862_, CallbackInfo ci) {
        if (DistValidate.isValid(worldIn)) {
            this.q = ((ServerLevel) worldIn);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V", at = @At("RETURN"))
    private void banner$init(ServerLevel p_196850_, ProtoChunk protoChunk, @Nullable LevelChunk.PostLoadProcessor p_196852_, CallbackInfo ci) {
        this.needsDecoration = true;
        this.banner$setPersistentDataContainer(protoChunk.bridge$persistentDataContainer()); // SPIGOT-6814: copy PDC to account for 1.17 to 1.18 chunk upgrading.
    }

    @Inject(method = "removeBlockEntity", at = @At(value = "INVOKE_ASSIGN", remap = false, target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"))
    private void banner$remove(BlockPos pos, CallbackInfo ci) {
        if (!pendingBlockEntities.isEmpty()) {
            pendingBlockEntities.remove(pos);
        }
    }

    @Override
    public org.bukkit.Chunk getBukkitChunk() {
        return new CraftChunk((LevelChunk) (Object) this);
    }

    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving, boolean doPlace) {
        this.banner$doPlace = doPlace;
        try {
            return this.setBlockState(pos, state, isMoving);
        } finally {
            this.banner$doPlace = true;
        }
    }

    @Override
    public void loadCallback() {
        org.bukkit.Server server = Bukkit.getServer();
        if (server != null) {
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
                random.setSeed(((ServerLevel) level).getSeed());
                long xRand = random.nextLong() / 2L * 2L + 1L;
                long zRand = random.nextLong() / 2L * 2L + 1L;
                random.setSeed((long) this.chunkPos.x * xRand + (long) this.chunkPos.z * zRand ^ ((ServerLevel) level).getSeed());

                org.bukkit.World world = this.level.getWorld();
                if (world != null) {
                    this.level.banner$setPopulating(true);
                    try {
                        for (org.bukkit.generator.BlockPopulator populator : world.getPopulators()) {
                            populator.populate(world, random, bukkitChunk);
                        }
                    } finally {
                        this.level.banner$setPopulating(false);
                    }
                }
                server.getPluginManager().callEvent(new org.bukkit.event.world.ChunkPopulateEvent(bukkitChunk));
            }
        }
    }

    @Override
    public void unloadCallback() {
        org.bukkit.Server server = Bukkit.getServer();
        var bukkitChunk = new CraftChunk((LevelChunk) (Object) this);
        org.bukkit.event.world.ChunkUnloadEvent unloadEvent = new org.bukkit.event.world.ChunkUnloadEvent(bukkitChunk, this.isUnsaved());
        server.getPluginManager().callEvent(unloadEvent);
        // note: saving can be prevented, but not forced if no saving is actually required
        this.mustNotSave = !unloadEvent.isSaveChunk();
    }

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private void banner$getState(BlockPos pos, BlockState state, boolean isMoving, CallbackInfoReturnable<BlockState> cir) {
        banner$block.set(state.getBlock());
    }

    @Redirect(method = "setBlockState", at = @At(value = "FIELD", ordinal = 1, target = "Lnet/minecraft/world/level/Level;isClientSide:Z"))
    public boolean banner$redirectIsRemote(Level world) {
        return world.isClientSide && this.banner$doPlace && (!this.level.bridge$captureBlockStates() || banner$block.get() instanceof BaseEntityBlock);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Nullable
    @Overwrite
    public BlockEntity getBlockEntity(BlockPos blockposition, LevelChunk.EntityCreationType creationType) {
        // CraftBukkit start
        BlockEntity tileentity = level.bridge$capturedTileEntities().get(blockposition);
        if (tileentity == null) {
            tileentity = (BlockEntity) this.blockEntities.get(blockposition);
        }
        // CraftBukkit end

        if (tileentity == null) {
            CompoundTag nbttagcompound = (CompoundTag) this.pendingBlockEntities.remove(blockposition);

            if (nbttagcompound != null) {
                BlockEntity tileentity1 = this.promotePendingBlockEntity(blockposition, nbttagcompound);

                if (tileentity1 != null) {
                    return tileentity1;
                }
            }
        }

        if (tileentity == null) {
            if (creationType == LevelChunk.EntityCreationType.IMMEDIATE) {
                tileentity = this.createBlockEntity(blockposition);
                if (tileentity != null) {
                    this.addAndRegisterBlockEntity(tileentity);
                }
            }
        } else if (tileentity.isRemoved()) {
            this.blockEntities.remove(blockposition);
            return null;
        }

        return tileentity;
    }

    @Inject(method = "setBlockEntity", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$addLogInfo(BlockEntity blockEntity, CallbackInfo ci, BlockPos blockPos, BlockEntity blockEntity2) {
        if (!this.getBlockState(blockPos).hasBlockEntity() && blockEntity2 == null && blockEntity2 == blockEntity) {
            System.out.println("Attempted to place a tile entity (" + blockEntity + ") at " + blockEntity.getBlockPos().getX() + "," + blockEntity.getBlockPos().getY() + "," + blockEntity.getBlockPos().getZ()
                    + " (" + getBlockState(blockPos) + ") where there was no entity tile!");
            System.out.println("Chunk coordinates: " + (this.chunkPos.x * 16) + "," + (this.chunkPos.z * 16));
            new Exception().printStackTrace();
            // CraftBukkit end
        }
    }

    @Override
    public boolean isUnsaved() {
        return super.isUnsaved() && !this.mustNotSave;
    }
}
