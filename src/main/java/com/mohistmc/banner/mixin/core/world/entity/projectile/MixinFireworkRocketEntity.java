package com.mohistmc.banner.mixin.core.world.entity.projectile;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public class MixinFireworkRocketEntity {

    @Inject(method = "explode", cancellable = true, at = @At("HEAD"))
    private void banner$fireworksExplode(CallbackInfo ci) {
        if (CraftEventFactory.callFireworkExplodeEvent((FireworkRocketEntity) (Object) this).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "dealExplosionDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void banner$damageSource(CallbackInfo ci) {
        CraftEventFactory.entityDamage = (FireworkRocketEntity) (Object) this;
    }

    @Inject(method = "dealExplosionDamage", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void banner$damageSourceReset(CallbackInfo ci) {
        CraftEventFactory.entityDamage = null;
    }
}
