package com.mohistmc.banner.mixin.world.level.block.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ConduitBlockEntity.class)
public class MixinConduitBlockEntity {

    @Redirect(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private static boolean banner$addEntity(Player player, MobEffectInstance eff) {
        player.pushEffectCause(EntityPotionEffectEvent.Cause.CONDUIT);
        return player.addEffect(eff);
    }
}
