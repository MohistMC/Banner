package com.mohistmc.banner.injection.world.item.crafting;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Map;

public interface InjectionRecipeManager {

    default Map<RecipeType<?>, Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>>> bridge$recipesCB() {
        return null;
    }

    default void addRecipe(RecipeHolder<?> irecipe) {
    }

    default boolean removeRecipe(ResourceLocation mcKey) {
        return false;
    }

    default void clearRecipes() {
    }
}
