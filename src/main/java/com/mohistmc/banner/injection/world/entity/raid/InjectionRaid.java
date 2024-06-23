package com.mohistmc.banner.injection.world.entity.raid;

import net.minecraft.world.entity.raid.Raider;

public interface InjectionRaid {

    default boolean isInProgress() {
        throw new IllegalStateException("Not implemented");
    }

    default java.util.Collection<Raider> getRaiders() {
        throw new IllegalStateException("Not implemented");
    }
}
