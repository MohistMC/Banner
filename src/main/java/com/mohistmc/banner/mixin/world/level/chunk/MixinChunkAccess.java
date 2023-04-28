package com.mohistmc.banner.mixin.world.level.chunk;

import com.mohistmc.banner.injection.world.level.chunk.InjectionChunkAccess;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.bukkit.craftbukkit.v1_19_R3.persistence.DirtyCraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ChunkAccess.class)
public abstract class MixinChunkAccess implements InjectionChunkAccess {

    @Shadow public abstract int getMinBuildHeight();

    @Shadow public abstract int getHeight();

    @Shadow public abstract LevelChunkSection getSection(int index);

    @Shadow @Final protected LevelChunkSection[] sections;
    // CraftBukkit start - SPIGOT-6814: move to IChunkAccess to account for 1.17 to 1.18 chunk upgrading.
    private static final org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY
            = new org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataTypeRegistry();
    public org.bukkit.craftbukkit.v1_19_R3.persistence.DirtyCraftPersistentDataContainer persistentDataContainer
            = new org.bukkit.craftbukkit.v1_19_R3.persistence.DirtyCraftPersistentDataContainer(DATA_TYPE_REGISTRY);
    // CraftBukkit end
    public Registry<Biome> biomeRegistry;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(ChunkPos p_187621_, UpgradeData p_187622_, LevelHeightAccessor p_187623_, Registry<Biome> registry, long p_187625_, LevelChunkSection[] p_187626_, BlendingData p_187627_, CallbackInfo ci) {
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
    public DirtyCraftPersistentDataContainer bridge$persistentDataContainer() {
        return persistentDataContainer;
    }

    @Override
    public Registry<Biome> bridge$biomeRegistry() {
        return biomeRegistry;
    }
}
