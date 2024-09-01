package com.mohistmc.banner.mixin.world.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import org.bukkit.projectiles.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrowableProjectile.class)
public abstract class MixinThrowableProjectile extends Projectile {

    public MixinThrowableProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void banner$init(EntityType<? extends ThrowableProjectile> type, LivingEntity livingEntityIn, Level worldIn, CallbackInfo ci) {
        this.banner$setProjectileSource((ProjectileSource) livingEntityIn.getBukkitEntity());
    }

    /*
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    private void banner$projectileHit(ThrowableProjectile entity, HitResult result) {
        this.preOnHit(result);
    }*/
}
