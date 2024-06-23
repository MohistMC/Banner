package com.mohistmc.banner.injection.world.entity;

public interface InjectionFishingHook {

    default int bridge$minLureTime() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setMinLureTime(int minLureTime) {
        throw new IllegalStateException("Not implemented");
    }

    default int bridge$maxLureTime() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setMaxLureTime(int maxLureTime) {
        throw new IllegalStateException("Not implemented");
    }

    default float bridge$minLureAngle() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setMinLureAnglee(float minLureAngle) {
        throw new IllegalStateException("Not implemented");
    }

    default float bridge$maxLureAngle() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setMaxLureAnglee(float maxLureAngle) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$rainInfluenced() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setRainInfluenced(boolean rainInfluenced) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$skyInfluenced() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setSkyInfluenced(boolean skyInfluenced) {
        throw new IllegalStateException("Not implemented");
    }

    default int bridge$minWaitTime() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setMinWaitTime(int minWaitTime) {
        throw new IllegalStateException("Not implemented");
    }

    default int bridge$maxWaitTime() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setMaxWaitTime(int minWaitTime) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$applyLure() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setApplyLure(boolean applyLure) {
        throw new IllegalStateException("Not implemented");
    }
}
