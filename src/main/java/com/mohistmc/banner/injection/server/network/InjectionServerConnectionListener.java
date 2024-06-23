package com.mohistmc.banner.injection.server.network;

public interface InjectionServerConnectionListener {

    default void acceptConnections() {
        throw new IllegalStateException("Not implemented");
    }
}
