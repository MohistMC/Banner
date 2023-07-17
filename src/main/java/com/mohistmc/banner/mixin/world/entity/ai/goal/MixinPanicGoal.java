package com.mohistmc.banner.mixin.world.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PanicGoal.class)
public abstract class MixinPanicGoal extends Goal {

    @Shadow @Final protected PathfinderMob mob;

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void banner$addCheckPanic(CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start - introduce a temporary timeout hack until this is fixed properly
        if ((this.mob.tickCount - this.mob.lastHurtByMobTimestamp) > 100) {
            this.mob.setLastHurtByMob((LivingEntity) null);
            cir.setReturnValue(false);
        }
        // CraftBukkit end
    }
}
