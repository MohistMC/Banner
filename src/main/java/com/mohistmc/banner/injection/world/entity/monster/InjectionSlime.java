package com.mohistmc.banner.injection.world.entity.monster;

public interface InjectionSlime {

    default boolean canWander() {
        return true;
    }

    default void setWander(boolean canWander) {
    }
}