package com.mohistmc.banner.mixin.world.effect;

import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.effect.RegenerationMobEffect")
public class MixinRegenerationMobEffect {

    @Inject(method = "applyEffectTick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
    private void banner$reason(LivingEntity livingEntity, int i, CallbackInfoReturnable<Boolean> cir) {
        livingEntity.pushHealReason(EntityRegainHealthEvent.RegainReason.MAGIC_REGEN);
    }
}
