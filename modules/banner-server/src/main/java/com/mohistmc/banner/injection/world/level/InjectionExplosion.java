package com.mohistmc.banner.injection.world.level;

public interface InjectionExplosion {

    default float bridge$getYield() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$wasCanceled() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setWasCanceled(boolean wasCanceled) {
        throw new IllegalStateException("Not implemented");
    }
}
