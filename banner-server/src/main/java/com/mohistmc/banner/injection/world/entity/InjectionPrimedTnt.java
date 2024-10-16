package com.mohistmc.banner.injection.world.entity;

public interface InjectionPrimedTnt {

    default float bridge$yield() {
        return 0;
    }

    default void banner$setYield(float yield) {
    }

    default boolean bridge$isIncendiary() {
        return false;
    }

    default void banner$setIsIncendiary(boolean isIncendiary) {
    }
}
