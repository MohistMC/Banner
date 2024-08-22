package com.mohistmc.banner.injection.network.connection;

import com.mojang.authlib.properties.Property;

import java.net.SocketAddress;
import java.util.UUID;

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

    default UUID bridge$spoofedUUID() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setSpoofedUUID(UUID spoofedUUID) {
        throw new IllegalStateException("Not implemented");
    }

    default Property[] bridge$spoofedProfile() {
        throw new IllegalStateException("Not implemented");
    }

    default void bridge$setSpoofedProfile(Property[] spoofedProfile) {
        throw new IllegalStateException("Not implemented");
    }
}
