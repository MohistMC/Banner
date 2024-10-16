package com.mohistmc.banner.injection.network.connection;

import java.net.SocketAddress;

public interface InjectionConnection {

    default String bridge$hostname() {
        return null;
    }

    default void banner$setHostName(String hostName) {

    }

    default SocketAddress getRawAddress() {
        return null;
    }
}
