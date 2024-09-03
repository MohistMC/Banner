package com.mohistmc.banner.mixin.world.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherSkull.class)
public abstract class MixinWitherSkull extends AbstractHurtingProjectile {

    protected MixinWitherSkull(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
    private void banner$heal(EntityHitResult result, CallbackInfo ci) {
        if (this.getOwner() != null) ((LivingEntity) this.getOwner()).pushHealReason(EntityRegainHealthEvent.RegainReason.WITHER);
    }

    @Inject(method = "onHitEntity", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$effect(EntityHitResult result, CallbackInfo ci) {
        ((LivingEntity) result.getEntity()).pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
    }

    @Redirect(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"))
    private Explosion banner$explode(Level world, Entity entityIn, double xIn, double yIn, double zIn, float explosionRadius, boolean causesFire, Level.ExplosionInteraction interaction) {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(this.getBukkitEntity(), explosionRadius, causesFire);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            return this.level().explode((WitherSkull) (Object) this, xIn, yIn, zIn, event.getRadius(), event.getFire(), interaction);
        }
        return null;
    }
}
