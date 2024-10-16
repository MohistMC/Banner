package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Parrot.class)
public abstract class MixinParrot extends ShoulderRidingEntity {

    protected MixinParrot(EntityType<? extends ShoulderRidingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Parrot;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private void arclight$feed(Player playerIn, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        pushEffectCause(EntityPotionEffectEvent.Cause.FOOD);
    }

}
