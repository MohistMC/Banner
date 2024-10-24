package com.mohistmc.banner.mixin.world.entity.ai.goal.target;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OwnerHurtByTargetGoal.class)
public abstract class MixinOwnerHurtByTargetGoal extends TargetGoal {

    public MixinOwnerHurtByTargetGoal(Mob mob, boolean bl) {
        super(mob, bl);
    }

    @Inject(method = "start", at = @At("HEAD"))
    public void banner$reason(CallbackInfo ci) {
        this.mob.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true);
    }
}
