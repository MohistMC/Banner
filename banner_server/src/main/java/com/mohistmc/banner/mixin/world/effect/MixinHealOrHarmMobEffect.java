package com.mohistmc.banner.mixin.world.effect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.effect.HealOrHarmMobEffect")
public class MixinHealOrHarmMobEffect {

    @Inject(method = "applyEffectTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
    private void banner$healReason1(LivingEntity livingEntity, int i, CallbackInfoReturnable<Boolean> cir) {
        livingEntity.pushHealReason(EntityRegainHealthEvent.RegainReason.MAGIC);
    }

    @Inject(method = "applyInstantenousEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
    private void banner$healReason1(Entity source, Entity indirectSource, LivingEntity livingEntity, int amplifier, double health, CallbackInfo ci) {
        livingEntity.pushHealReason(EntityRegainHealthEvent.RegainReason.MAGIC);
    }
}
