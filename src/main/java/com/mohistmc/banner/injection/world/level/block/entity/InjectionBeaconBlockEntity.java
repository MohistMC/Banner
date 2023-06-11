package com.mohistmc.banner.injection.world.level.block.entity;

import org.bukkit.potion.PotionEffect;

public interface InjectionBeaconBlockEntity {

    default PotionEffect getPrimaryEffect() {
        return null;
    }

    default PotionEffect getSecondaryEffect() {
        return null;
    }
}
