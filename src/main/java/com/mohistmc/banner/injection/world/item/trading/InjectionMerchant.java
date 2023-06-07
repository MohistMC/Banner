package com.mohistmc.banner.injection.world.item.trading;

import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMerchant;

public interface InjectionMerchant {

    default CraftMerchant getCraftMerchant() {
        return null;
    }
}
