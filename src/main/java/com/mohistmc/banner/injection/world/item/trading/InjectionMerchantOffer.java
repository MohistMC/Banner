package com.mohistmc.banner.injection.world.item.trading;

import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftMerchantRecipe;

public interface InjectionMerchantOffer {

    default CraftMerchantRecipe asBukkit() {
        return null;
    }

}
