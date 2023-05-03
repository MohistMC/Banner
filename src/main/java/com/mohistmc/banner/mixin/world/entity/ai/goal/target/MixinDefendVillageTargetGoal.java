package com.mohistmc.banner.mixin.world.entity.ai.goal.target;

import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefendVillageTargetGoal.class)
public class MixinDefendVillageTargetGoal {

    @Shadow @Final private IronGolem golem;

    @Inject(method = "start", at = @At("HEAD"))
    public void banner$reason(CallbackInfo ci) {
        this.golem.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.DEFEND_VILLAGE, true);
    }
}
