package com.mohistmc.banner.mixin.world.entity.moster;

import com.mohistmc.banner.injection.world.entity.InjectionGuardian;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Guardian.class)
public class MixinGuardian implements InjectionGuardian {

    public Guardian.GuardianAttackGoal guardianAttackGoal;

    @ModifyArg(method = "registerGoals", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V"))
    private Goal banner$saveGoal(Goal goal) {
        if (goal instanceof Guardian.GuardianAttackGoal guardianGoal) {
            this.guardianAttackGoal = guardianGoal;
        }
        return goal;
    }

    @Override
    public Guardian.GuardianAttackGoal bridge$guardianAttackGoal() {
        return guardianAttackGoal;
    }

    @Override
    public void banner$setGuardianAttackGoal(Guardian.GuardianAttackGoal guardianAttackGoal) {
        this.guardianAttackGoal = guardianAttackGoal;
    }
}
