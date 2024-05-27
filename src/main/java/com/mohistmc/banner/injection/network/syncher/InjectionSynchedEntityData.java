package com.mohistmc.banner.injection.network.syncher;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;

public interface InjectionSynchedEntityData {

    default <T> void markDirty(EntityDataAccessor<T> datawatcherobject) {
    }
}
