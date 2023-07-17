package com.mohistmc.banner.injection.world.item.crafting;

public interface InjectionShapedRecipe {

    default org.bukkit.inventory.ShapedRecipe toBukkitRecipe() {
        return null;
    }

}
