package com.mohistmc.banner.mixin.world.entity.ai.goal.target;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OwnerHurtTargetGoal.class)
public abstract class MixinOwnerHurtTargetGoal extends TargetGoal {

    public MixinOwnerHurtTargetGoal(Mob mob, boolean bl) {
        super(mob, bl);
    }

    @Inject(method = "start", at = @At("HEAD"))
    public void banner$reason(CallbackInfo ci) {
         this.mob.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
    }
}
