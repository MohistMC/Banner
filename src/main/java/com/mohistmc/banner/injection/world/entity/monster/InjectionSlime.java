package com.mohistmc.banner.injection.world.entity.monster;

public interface InjectionSlime {

    default boolean canWander() {
        throw new IllegalStateException("Not implemented");
    }

    default void setWander(boolean canWander) {
        throw new IllegalStateException("Not implemented");
    }
}
