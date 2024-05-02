package com.mohistmc.banner.injection.server.network;

import net.minecraft.server.level.ServerPlayer;

public interface InjectionServerCommonPacketListenerImpl {

    default void disconnect(String s) {
    }

    default boolean bridge$processedDisconnect() {
        return false;
    }

    default boolean banner$isDisconnected() {
        return false;
    }

    default void banner$setPlayer(ServerPlayer player) {
    }

    default void banner$setProcessedDisconnect(boolean processedDisconnect) {
    }
}
