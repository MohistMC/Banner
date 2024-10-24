package com.mohistmc.banner.injection.server.level;

import java.util.Set;
import net.minecraft.server.network.ServerPlayerConnection;

public interface InjectionServerEntity {

    default void setTrackedPlayers(Set<ServerPlayerConnection> trackedPlayers) {
        throw new IllegalStateException("Not implemented");
    }
}
