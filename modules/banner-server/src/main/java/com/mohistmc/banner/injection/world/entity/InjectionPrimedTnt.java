package com.mohistmc.banner.injection.world.entity;

public interface InjectionPrimedTnt {

    default float bridge$yield() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setYield(float yield) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$isIncendiary() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setIsIncendiary(boolean isIncendiary) {
        throw new IllegalStateException("Not implemented");
    }
}
