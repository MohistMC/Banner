package com.mohistmc.banner.injection.server.level;

import net.minecraft.server.network.ServerPlayerConnection;

import java.util.Set;

public interface InjectionServerEntity {

    default void setTrackedPlayers(Set<ServerPlayerConnection> trackedPlayers) {
        throw new IllegalStateException("Not implemented");
    }
}
