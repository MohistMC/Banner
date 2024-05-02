package com.mohistmc.banner.injection.world.item.crafting;

public interface InjectionRecipeHolder {

    default org.bukkit.inventory.Recipe toBukkitRecipe() {
        return null;
    }
}
