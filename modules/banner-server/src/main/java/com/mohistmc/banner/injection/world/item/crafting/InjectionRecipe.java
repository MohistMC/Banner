package com.mohistmc.banner.injection.world.item.crafting;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;

public interface InjectionRecipe {

    default Recipe toBukkitRecipe(NamespacedKey id) {
        throw new IllegalStateException("Not implemented");
    }
}
