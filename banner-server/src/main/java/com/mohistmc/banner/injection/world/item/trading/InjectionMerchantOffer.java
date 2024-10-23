package com.mohistmc.banner.injection.world.item.trading;

import org.bukkit.craftbukkit.inventory.CraftMerchantRecipe;

public interface InjectionMerchantOffer {

    default CraftMerchantRecipe asBukkit() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCraftMerchantRecipe(CraftMerchantRecipe merchantRecipe) {
        throw new IllegalStateException("Not implemented");
    }
}
