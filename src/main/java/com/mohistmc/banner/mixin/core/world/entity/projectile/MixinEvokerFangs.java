package com.mohistmc.banner.mixin.core.world.entity.projectile;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EvokerFangs.class)
public class MixinEvokerFangs {

    @Inject(method = "dealDamageTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void banner$entityDamage(LivingEntity target, CallbackInfo ci) {
        CraftEventFactory.entityDamage = (EvokerFangs) (Object) this;
    }

    @Inject(method = "dealDamageTo", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void banner$entityDamageReset(LivingEntity target, CallbackInfo ci) {
        CraftEventFactory.entityDamage = null;
    }
}
