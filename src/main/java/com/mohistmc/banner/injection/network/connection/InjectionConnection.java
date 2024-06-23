package com.mohistmc.banner.injection.network.connection;

import java.net.SocketAddress;

public interface InjectionConnection {

    default String bridge$hostname() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setHostName(String hostName) {
        throw new IllegalStateException("Not implemented");
    }

    default SocketAddress getRawAddress() {
        throw new IllegalStateException("Not implemented");
    }
}
