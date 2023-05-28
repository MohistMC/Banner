package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.world.entity.monster.Phantom;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomAttackPlayerTargetGoal")
public class MixinPhantom_AttackPlayerTargetGoal {

    @SuppressWarnings("target") @Shadow(aliases = {"field_7319"}, remap = false)
    private Phantom outerThis;

    // canUse  setTarget
    @Inject(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Phantom;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void banner$reason(CallbackInfoReturnable<Boolean> cir) {
        outerThis.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
    }
}
