package com.mohistmc.banner.mixin.core.world.damagesource;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mohistmc.banner.injection.world.damagesource.InjectionDamageSources;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSources.class)
public abstract class MixinDamageSources implements InjectionDamageSources {

    // @formatter:off
    @Shadow protected abstract DamageSource source(ResourceKey<DamageType> resourceKey);
    // @formatter:on

    @Shadow public abstract DamageSource source(ResourceKey<DamageType> resourceKey, @Nullable Entity entity, @Nullable Entity entity2);

    @Shadow @Final public Registry<DamageType> damageTypes;
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
    public DamageSource melting() {
        return melting;
    }

    @Override
    public DamageSource poison() {
        return poison;
    }
    // Banner end

    /**
     * @author wdog5
     * @reason bukkit patch
     */
    @Overwrite
    public DamageSource explosion(@Nullable Entity entity, @Nullable Entity entity2) {
        return this.explosion(entity, entity2, entity2 != null && entity != null ? DamageTypes.PLAYER_EXPLOSION : DamageTypes.EXPLOSION);
    }

    /**
     * @author wdog5
     * @reason bukkit patch
     */
    @Overwrite
    public DamageSource badRespawnPointExplosion(Vec3 vec3) {
        return badRespawnPointExplosion(vec3, null);
    }

    @Override
    public DamageSource explosion(@Nullable Entity entity, @Nullable Entity entity1, ResourceKey<DamageType> resourceKey) {
        return this.source(resourceKey, entity, entity1);
    }

    @Override
    public DamageSource badRespawnPointExplosion(Vec3 vec3d, BlockState blockState) {
        return new DamageSource(this.damageTypes.getHolderOrThrow(DamageTypes.BAD_RESPAWN_POINT), vec3d).directBlockState(blockState);
    }
}
