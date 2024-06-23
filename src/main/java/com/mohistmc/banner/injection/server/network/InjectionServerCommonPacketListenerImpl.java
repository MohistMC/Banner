package com.mohistmc.banner.injection.server.network;

import net.minecraft.server.level.ServerPlayer;

public interface InjectionServerCommonPacketListenerImpl {

    default void disconnect(String s) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$processedDisconnect() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean banner$isDisconnected() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setPlayer(ServerPlayer player) {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setProcessedDisconnect(boolean processedDisconnect) {
        throw new IllegalStateException("Not implemented");
    }
}
