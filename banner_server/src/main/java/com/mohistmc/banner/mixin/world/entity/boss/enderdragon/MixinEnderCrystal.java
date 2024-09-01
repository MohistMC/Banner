package com.mohistmc.banner.mixin.world.entity.boss.enderdragon;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystal.class)
public abstract class MixinEnderCrystal extends Entity {

    public MixinEnderCrystal(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }


    @Inject(method = "tick", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private void banner$blockIgnite(CallbackInfo ci) {
        if (CraftEventFactory.callBlockIgniteEvent(this.level(), this.blockPosition(), (EndCrystal) (Object) this).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"))
    private void banner$entityDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent((EndCrystal) (Object) this, source, amount)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"))
    private Explosion banner$blockPrime(Level world, Entity entityIn, DamageSource damageSource, ExplosionDamageCalculator calculator, double xIn, double yIn, double zIn, float explosionRadius, boolean fire, Level.ExplosionInteraction interaction) {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(this.getBukkitEntity(), explosionRadius, fire);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            this.unsetRemoved();
            return null;
        } else {
            return world.explode(entityIn, damageSource, calculator, xIn, yIn, zIn, event.getRadius(), event.getFire(), interaction);
        }
    }
}
