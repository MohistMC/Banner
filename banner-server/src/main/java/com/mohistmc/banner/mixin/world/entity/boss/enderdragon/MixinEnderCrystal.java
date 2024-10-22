package com.mohistmc.banner.mixin.world.entity.boss.enderdragon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
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

    @Inject(method = "hurtServer", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"))
    private void banner$entityDamage(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent((EndCrystal) (Object) this, damageSource, f)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"))
    private void banner$blockPrime(ServerLevel instance, Entity entity, DamageSource damageSource, ExplosionDamageCalculator explosionDamageCalculator, double v, double v, double v, float v, boolean b, ExplosionInteraction explosionInteraction) {
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
