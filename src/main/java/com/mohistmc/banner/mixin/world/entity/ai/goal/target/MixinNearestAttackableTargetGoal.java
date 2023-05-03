package com.mohistmc.banner.mixin.world.entity.ai.goal.target;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class MixinNearestAttackableTargetGoal extends TargetGoal {

    @Shadow protected LivingEntity target;

    public MixinNearestAttackableTargetGoal(Mob mob, boolean bl) {
        super(mob, bl);
    }

    @Inject(method = "start", at = @At("HEAD"))
    public void banner$reason(CallbackInfo ci) {
        this.mob.bridge$pushGoalTargetReason(this.target instanceof ServerPlayer ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
    }
}
