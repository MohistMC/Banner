package com.mohistmc.banner.injection.world.entity;

import net.minecraft.world.entity.monster.Guardian;

public interface InjectionGuardian {

    default Guardian.GuardianAttackGoal bridge$guardianAttackGoal() {
        return null;
    }

    default void banner$setGuardianAttackGoal(Guardian.GuardianAttackGoal guardianAttackGoal) {
    }
}
