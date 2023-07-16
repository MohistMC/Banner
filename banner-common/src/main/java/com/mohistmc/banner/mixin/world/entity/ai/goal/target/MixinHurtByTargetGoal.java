package com.mohistmc.banner.mixin.world.entity.ai.goal.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HurtByTargetGoal.class)
public abstract class MixinHurtByTargetGoal extends TargetGoal {

    public MixinHurtByTargetGoal(Mob mob, boolean bl) {
        super(mob, bl);
    }

    @Inject(method = "start", at = @At("HEAD"))
    public void banner$reason1(CallbackInfo ci) {
         this.mob.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY, true);
    }

    @Inject(method = "alertOther", at = @At("HEAD"))
    public void banner$reason2(Mob mobIn, LivingEntity targetIn, CallbackInfo ci) {
         mobIn.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
    }
}
