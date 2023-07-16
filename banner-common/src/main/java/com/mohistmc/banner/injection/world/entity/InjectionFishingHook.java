package com.mohistmc.banner.injection.world.entity;

public interface InjectionFishingHook {

    default int bridge$minLureTime() {
        return 0;
    }

    default void banner$setMinLureTime(int minLureTime) {}

    default int bridge$maxLureTime() {
        return 0;
    }

    default void banner$setMaxLureTime(int maxLureTime) {}

    default float bridge$minLureAngle() {
        return 0;
    }

    default void banner$setMinLureAnglee(float minLureAngle) {}

    default float bridge$maxLureAngle() {
        return 0;
    }

    default void banner$setMaxLureAnglee(float maxLureAngle) {}

    default boolean bridge$rainInfluenced() {
        return false;
    }

    default void banner$setRainInfluenced(boolean rainInfluenced) {}

    default boolean bridge$skyInfluenced() {
        return false;
    }

    default void banner$setSkyInfluenced(boolean skyInfluenced) {}

    default int bridge$minWaitTime() {
        return 0;
    }

    default void banner$setMinWaitTime(int minWaitTime) {}

    default int bridge$maxWaitTime() {
        return 0;
    }

    default void banner$setMaxWaitTime(int minWaitTime) {}

    default boolean bridge$applyLure() {
        return false;
    }

    default void banner$setApplyLure(boolean applyLure) {}
}
