package com.mohistmc.banner.injection.network.connection;

public interface InjectionConnection {

    default String bridge$hostname() {
        return null;
    }

    default void banner$setHostName(String hostName) {

    }
}
