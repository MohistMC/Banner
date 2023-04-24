package com.mohistmc.banner.injection.world.level;

public interface InjectionExplosion {

    default boolean bridge$wasCanceled() {
        return false;
    }

    default void banner$setWasCanceled(boolean wasCanceled) {
    }
}
