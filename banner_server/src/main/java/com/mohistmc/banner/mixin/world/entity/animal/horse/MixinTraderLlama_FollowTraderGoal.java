package com.mohistmc.banner.mixin.world.entity.animal.horse;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.entity.animal.horse.TraderLlama.TraderLlamaDefendWanderingTraderGoal.class)
public abstract class MixinTraderLlama_FollowTraderGoal extends TargetGoal {

    public MixinTraderLlama_FollowTraderGoal(Mob mob, boolean bl) {
        super(mob, bl);
    }

    @Inject(method = "start", at = @At("HEAD"))
    private void banner$reason(CallbackInfo ci) {
         this.mob.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true);
    }
}
