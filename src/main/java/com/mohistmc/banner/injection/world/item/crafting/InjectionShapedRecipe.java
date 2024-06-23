package com.mohistmc.banner.injection.world.item.crafting;

import org.bukkit.NamespacedKey;

public interface InjectionShapedRecipe {

    default org.bukkit.inventory.ShapedRecipe toBukkitRecipe(NamespacedKey id) {
        throw new IllegalStateException("Not implemented");
    }

}
