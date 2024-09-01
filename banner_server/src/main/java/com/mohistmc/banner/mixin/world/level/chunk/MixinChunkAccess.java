package com.mohistmc.banner.mixin.world.level.chunk;

import com.mohistmc.banner.injection.world.level.chunk.InjectionChunkAccess;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.persistence.DirtyCraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkAccess.class)
public abstract class MixinChunkAccess implements BlockGetter, BiomeManager.NoiseBiomeSource, LightChunk, StructureAccess, InjectionChunkAccess {

    @Shadow public abstract int getHeight();

    @Shadow @Final protected LevelChunkSection[] sections;

    @Shadow public abstract int getMinBuildHeight();

    // CraftBukkit start - SPIGOT-6814: move to IChunkAccess to account for 1.17 to 1.18 chunk upgrading.
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
    public DirtyCraftPersistentDataContainer persistentDataContainer = new DirtyCraftPersistentDataContainer(DATA_TYPE_REGISTRY);
    // CraftBukkit end
    public Registry<Biome> biomeRegistry;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome>  registry, long l, LevelChunkSection[] levelChunkSections, BlendingData blendingData, CallbackInfo ci) {
        this.biomeRegistry = registry;
    }

    @Inject(method = "setUnsaved", at = @At("HEAD"))
    private void banner$dirty(boolean flag, CallbackInfo ci) {
        if (!flag) {
            this.persistentDataContainer.dirty(false);
        }
    }

    @Inject(method = "isUnsaved", cancellable = true, at = @At("RETURN"))
    private void banner$isDirty(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() || this.persistentDataContainer.dirty());
    }

    @Override
    public void setBiome(int i, int j, int k, Holder<Biome> biome) {
        try {
            int l = QuartPos.fromBlock(this.getMinBuildHeight());
            int i1 = l + QuartPos.fromBlock(this.getHeight()) - 1;
            int j1 = Mth.clamp(j, l, i1);
            int k1 = this.getSectionIndex(QuartPos.toBlock(j1));

            this.sections[k1].setBiome(i & 3, j1 & 3, k & 3, biome);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Setting biome");
            CrashReportCategory crashreportsystemdetails = crashreport.addCategory("Biome being set");

            crashreportsystemdetails.setDetail("Location", () -> {
                return CrashReportCategory.formatLocation(((ChunkAccess) (Object) this), i, j, k);
            });
            throw new ReportedException(crashreport);
        }
    }

    @Override
    public DirtyCraftPersistentDataContainer bridge$persistentDataContainer() {
        return persistentDataContainer;
    }

    @Override
    public Registry<Biome> bridge$biomeRegistry() {
        return biomeRegistry;
    }
}
