package com.mohistmc.banner.injection.world.entity.raid;

import net.minecraft.world.entity.raid.Raider;

public interface InjectionRaid {

    default boolean isInProgress() {
        return false;
    }

    default java.util.Collection<Raider> getRaiders() {
        return null;
    }
}
