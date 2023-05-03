package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Guardian.class)
public class MixinGuardian {

    public Guardian.GuardianAttackGoal guardianAttackGoal;

    @ModifyArg(method = "registerGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V"))
    private Goal banner$saveGoal(Goal goal) {
        if (goal instanceof Guardian.GuardianAttackGoal guardianGoal) {
            this.guardianAttackGoal = guardianGoal;
        }
        return goal;
    }
}
