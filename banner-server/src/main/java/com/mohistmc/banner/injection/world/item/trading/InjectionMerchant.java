package com.mohistmc.banner.injection.world.item.trading;

import org.bukkit.craftbukkit.inventory.CraftMerchant;

public interface InjectionMerchant {

    default CraftMerchant getCraftMerchant() {
        throw new IllegalStateException("Not implemented");
    }
}
