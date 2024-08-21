package com.mohistmc.banner.injection.network.syncher;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;

public interface InjectionSynchedEntityData {

    default <T> void markDirty(EntityDataAccessor<T> datawatcherobject) {
        throw new IllegalStateException("Not implemented");
    }

    default void refresh(ServerPlayer player) {
        throw new IllegalStateException("Not implemented");
    }
}
