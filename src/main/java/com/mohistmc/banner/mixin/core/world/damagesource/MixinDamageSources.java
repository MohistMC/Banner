package com.mohistmc.banner.mixin.core.world.damagesource;

import com.mohistmc.banner.injection.world.damagesource.InjectionDamageSources;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageSources.class)
public abstract class MixinDamageSources implements InjectionDamageSources {

    // @formatter:off
    @Shadow protected abstract DamageSource source(ResourceKey<DamageType> resourceKey);
    // @formatter:on

    // CraftBukkit start
    public DamageSource melting;
    public DamageSource poison;

    @Inject(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/DamageSources;" +
                    "source(Lnet/minecraft/resources/ResourceKey;)" +
                    "Lnet/minecraft/world/damagesource/DamageSource;", shift = At.Shift.BEFORE, ordinal = 0))
    private void banner$init(RegistryAccess registryAccess, CallbackInfo ci) {
        this.melting = this.source(DamageTypes.ON_FIRE).melting();
        this.poison = this.source(DamageTypes.MAGIC).poison();
        // CraftBukkit end
    }

    // Banner start
    @Override
    public DamageSource bridge$melting() {
        return melting;
    }

    @Override
    public DamageSource bridge$poison() {
        return poison;
    }
    // Banner end
}
