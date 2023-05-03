package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pufferfish.class)
public class MixinPufferfish {

    @Inject(method = "touch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$attack(Mob mobEntity, CallbackInfo ci) {
         mobEntity.pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$collide(Player entityIn, CallbackInfo ci) {
         entityIn.pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
    }
}
