package com.mohistmc.banner.injection.world.entity;

public interface InjectionAgeableMob {

    default boolean bridge$ageLocked() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setAgeLocked(boolean ageLocked) {
        throw new IllegalStateException("Not implemented");
    }
}
