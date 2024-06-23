package com.mohistmc.banner.injection.world.entity;

import net.minecraft.world.entity.animal.allay.Allay;

public interface InjectionAllay {

    default Allay duplicateAllay0() {
        throw new IllegalStateException("Not implemented");
    }

    default void setCanDuplicate(boolean canDuplicate) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$forceDancing() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setForceDancing(boolean forceDancing) {
        throw new IllegalStateException("Not implemented");
    }
}
