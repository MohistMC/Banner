package com.mohistmc.banner.injection.world.item.crafting;

public interface InjectionShapelessRecipe {

    default org.bukkit.inventory.ShapelessRecipe toBukkitRecipe() {
        return null;
    }
}
