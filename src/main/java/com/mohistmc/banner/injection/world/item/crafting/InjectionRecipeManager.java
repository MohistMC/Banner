package com.mohistmc.banner.injection.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public interface InjectionRecipeManager {

    default void addRecipe(Recipe<?> irecipe) {
    }

    default boolean removeRecipe(ResourceLocation mcKey) {
        return false;
    }

    default void clearRecipes() {
    }
}
