package com.mohistmc.banner.mixin.world;

import net.fabricmc.fabric.impl.biome.modification.BiomeModificationImpl;
import net.fabricmc.fabric.impl.biome.modification.BiomeModificationMarker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BiomeModificationImpl.class, remap = false)
public abstract class MixinBiomeModificationImpl {

    @Redirect(method = "finalizeWorldGen",
            at = @At(value = "INVOKE",
                    target = "Lnet/fabricmc/fabric/impl/biome/modification/BiomeModificationMarker;fabric_markModified()V"))
    private void banner$markNone(BiomeModificationMarker instance) {
        // Banner do not thing to modify
    }
}