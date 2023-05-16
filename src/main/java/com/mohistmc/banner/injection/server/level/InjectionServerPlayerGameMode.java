package com.mohistmc.banner.injection.server.level;

public interface InjectionServerPlayerGameMode {

    default boolean bridge$isFiredInteract() {
        return false;
    }

    default void bridge$setFiredInteract(boolean firedInteract) {
    }

    default boolean bridge$getInteractResult() {
        return false;
    }
}
