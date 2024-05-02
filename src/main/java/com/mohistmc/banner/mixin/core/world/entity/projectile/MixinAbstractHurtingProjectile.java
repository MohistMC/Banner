package com.mohistmc.banner.mixin.core.world.entity.projectile;

import com.mohistmc.banner.injection.world.entity.projectile.InjectionAbstractHurtingProjectile;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractHurtingProjectile.class)
public abstract class MixinAbstractHurtingProjectile extends Projectile implements InjectionAbstractHurtingProjectile {

    @Shadow public double xPower;
    @Shadow public double yPower;
    @Shadow public double zPower;
    public float bukkitYield = 1; // CraftBukkit
    public boolean isIncendiary = true; // CraftBukkit

    public MixinAbstractHurtingProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void banner$init(EntityType<?> entityType, Level level, CallbackInfo ci) {
        this.bukkitYield = 1;
        this.isIncendiary = true;
    }

    @Override
    public void setDirection(double d0, double d1, double d2) {
        double banner$d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        this.xPower = d0 / banner$d3 * 0.1D;
        this.yPower = d1 / banner$d3 * 0.1D;
        this.zPower = d2 / banner$d3 * 0.1D;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractHurtingProjectile;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    private void banner$preOnHit(AbstractHurtingProjectile abstractHurtingProjectile, HitResult hitResult) {
        this.preOnHit(hitResult);
    }

    @Inject(method = "tick", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/projectile/AbstractHurtingProjectile;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    private void banner$projectileHit(CallbackInfo ci, Entity entity, HitResult rayTraceResult) {
        if (this.isRemoved()) {
            CraftEventFactory.callProjectileHitEvent((AbstractHurtingProjectile) (Object) this, rayTraceResult);
        }
    }

    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"))
    private void banner$nonLivingAttack(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent((AbstractHurtingProjectile) (Object) this, source, amount, false)) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public float bridge$bukkitYield() {
        return bukkitYield;
    }

    @Override
    public boolean bridge$isIncendiary() {
        return isIncendiary;
    }

    @Override
    public void banner$setBukkitYield(float yield) {
        bukkitYield = yield;
    }

    @Override
    public void banner$setIsIncendiary(boolean incendiary) {
        isIncendiary = incendiary;
    }
}
