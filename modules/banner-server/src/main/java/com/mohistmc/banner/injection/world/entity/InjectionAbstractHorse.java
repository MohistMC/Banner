package com.mohistmc.banner.injection.world.entity;

public interface InjectionAbstractHorse {

    default int bridge$maxDomestication() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setMaxDomestication(int maxDomestication) {
        throw new IllegalStateException("Not implemented");
    }
}
