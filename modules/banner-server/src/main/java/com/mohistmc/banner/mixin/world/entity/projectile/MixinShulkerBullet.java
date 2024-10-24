package com.mohistmc.banner.mixin.world.entity.projectile;

import com.mohistmc.banner.injection.world.entity.projectile.InjectionShulkerBullet;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBullet.class)
public abstract class MixinShulkerBullet extends Projectile implements InjectionShulkerBullet {

    @Shadow @Nullable private Entity finalTarget;

    @Shadow @Nullable private Direction currentMoveDirection;

    public MixinShulkerBullet(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected abstract void selectNextMoveDirection(@Nullable Direction.Axis axis);

    @Override
    public Entity getTarget() {
        return this.finalTarget;
    }

    @Override
    public void setTarget(Entity e) {
        this.finalTarget = e;
        this.currentMoveDirection = Direction.UP;
        this.selectNextMoveDirection(Direction.Axis.X);
    }


    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Direction$Axis;)V", at = @At("RETURN"))
    private void banner$init(Level level, LivingEntity livingEntity, Entity entity, Direction.Axis axis, CallbackInfo ci) {
        this.banner$setProjectileSource((ProjectileSource) entity.getBukkitEntity());
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$reason(EntityHitResult result, CallbackInfo ci) {
        ((LivingEntity) result.getEntity()).pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void banner$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent(this, source, amount, false)) {
            cir.setReturnValue(false);
        }
    }
}
