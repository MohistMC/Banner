package com.mohistmc.banner.injection.world.entity;

public interface InjectionLightningBolt {

    default boolean bridge$isSilent() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setIsSilent(boolean isSilent) {
        throw new IllegalStateException("Not implemented");
    }
}
