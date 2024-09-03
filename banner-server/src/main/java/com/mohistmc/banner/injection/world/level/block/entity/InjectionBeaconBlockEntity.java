package com.mohistmc.banner.injection.world.level.block.entity;

import org.bukkit.potion.PotionEffect;

public interface InjectionBeaconBlockEntity {

    default PotionEffect getPrimaryEffect() {
        throw new IllegalStateException("Not implemented");
    }

    default PotionEffect getSecondaryEffect() {
        throw new IllegalStateException("Not implemented");
    }
}
