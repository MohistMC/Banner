package com.mohistmc.banner.mixin.world.entity.projectile;

import com.mohistmc.banner.injection.world.entity.projectile.InjectionAbstractHurtingProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Banner TODO fixme
@Mixin(AbstractHurtingProjectile.class)
public abstract class MixinAbstractHurtingProjectile extends Projectile implements InjectionAbstractHurtingProjectile {

    @Shadow public abstract void assignDirectionalMovement(Vec3 vec3, double d);

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
        Vec3 vec = new Vec3(d0 / banner$d3 * 0.1D, d1 / banner$d3 * 0.1D, 2 / banner$d3 * 0.1D);
        this.assignDirectionalMovement(vec, banner$d3);
    }

    /*
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
    }*/

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
